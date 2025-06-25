#!/usr/bin/env python3

# Copyright (c) 2020 Intel Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# coding: utf-8
# our image is contained as parts, including the hash
# then it gets zipped up and signed again

# this internal signature is for boot and recovery, but
# will be checked prior to writing to flash as well.

# the internal signature format is a PFR-specific block
# including a hash bitmap, certificates (public keys),
# and the actual signature data as well, for both active
# and recovery images

import os, hashlib, struct, json, sys, subprocess, mmap, io, array, binascii, copy, shutil, re, getopt, argparse
from array import array
from binascii import unhexlify
from hashlib import sha1, sha256, sha384, sha512
from shutil import copyfile
# Flash Map
# -----------------------------------------------
# Start addr      Contents
# 0x00000000  S   U-Boot
# 0x00080000  S+  PFM
# 0x000a0000  U   U-boot Env
# 0x000C0000  U   SOFS
# 0x002c0000  U   RWFS
# 0x00b00000  S   fit-image
# 0x02a00000  S+  rc-image
# 0x04a00000  U   staging-image
# * partially signed (not full 64k page)
# + unsigned, owned by pfr

# define constants
PFM_SPI = 0x1  # SPI rule Type
PFM_I2C = 0x2  # I2C rule type
SHA256  = 0x1  # hash256 present
SHA384  = 0x2  # hash384 present
SHA256_SIZE = 32  # Hash 256 size
PFM_DEF_SIZE = 32 # 32 bytes of PFM header
PFM_SPI_SIZE_DEF = 16 # 16 bytes of SPI PFM
PFM_SPI_SIZE_HASH = 32 # 32 bytes of SPI region HASH
PFM_I2C_SIZE = 40  # 40 bytes of i2c rules region in PFM
PAGE_SIZE = 0x1000 # 4KB size of page

def load_manifest(fname):
    manifest = {}
    with open(fname, 'r') as fd:
        manifest = json.load(fd)
    return manifest

class pfm_spi(object):

    def __init__(self, prot_mask, start_addr, end_addr, hash, hash_pres):
        self.spi_pfm = PFM_SPI
        self.spi_prot_mask = prot_mask
        self.spi_hash_pres = hash_pres
        print("hash_pres={}".format(self.spi_hash_pres))
        print("spi_hash={}".format(hash))
        print("spi_start_addr={}".format(hex(start_addr)))
        print("spi_end_addr={}".format(hex(end_addr)))
        if hash_pres != 0:
            self.spi_hash = hash
        self.spi_pfm_rsvd = 0xffffffff        # b'\xff'*4
        self.spi_start_addr = start_addr
        self.spi_end_addr = end_addr

class pfm_i2c(object):

    def __init__(self, bus_id, rule_id, address, cmd_map):
        self.i2c_pfm = PFM_I2C
        self.i2c_pfm_rsvd = 0xffffffff        # b'\xff'*4
        self.i2c_bus_id = bus_id
        self.i2c_rule_id = rule_id
        self.i2c_address = address
        self.i2c_cmd_whitelist = cmd_map

class pfr_bmc_image(object):
    """class of PFR bmc to create pfm, and capsule """
    # json_file, firmware_file
    def __init__(self, platform, manifest, firmware_file, build_maj, build_min, build_num, svn=1, bkc_ver=1, sha_algo="2", output_filename="image-mtd-pfr"):

        self.platform_name = platform
        self.manifest = load_manifest(manifest)
        self.firmware_file = firmware_file
        self.build_ver_maj = build_maj
        self.build_ver_min = build_min
        self.build_number = build_num
        self.sec_rev = svn
        self.bkc_ver = bkc_ver
        self.sha = sha_algo

        if self.sha == "2":
            self.sha_version = 0x2
            self.pfm_spi_size_hash = 48
        if self.sha == "1":
            self.pfm_spi_size_hash = 32
            self.sha_version = 0x1
        self.pfr_rom_file = output_filename
        open(self.pfr_rom_file, 'a').close()

        self.page_size = PAGE_SIZE
        self.empty = b'\xff' * self.page_size

        self.image_parts = []
        for p in self.manifest['image-parts']:
            # the json should have in the order- filename, index, offset, size and protection byte
            self.image_parts.append((p['name'], p['index'], p['offset'], p['size'], p['prot_mask'], p['pfm'], p['hash'], p['compress']))
        if self.sha == "1":
            self.act_dgst = hashlib.sha256()
        if self.sha == "2":
            self.act_dgst = hashlib.sha384()
        # SPI regions PFM array
        self.pfm_spi_regions = []
        self.pfm_bytes = PFM_DEF_SIZE # PFM definition bytes (SPI regions + SMBUS)

        # hash, erase and compression bit maps for 128MB or 256 MB
        max_offset = max(int(p["offset"], 16) + int(p["size"], 16) for p in self.manifest["image-parts"])
        if max_offset > 0x8000000:
            self.pbc_erase_bitmap = bytearray(8192)
            self.pbc_comp_bitmap = bytearray(8192)
            self.bitmap_size = 0x10000
        else:
            self.pbc_erase_bitmap = bytearray(4096)
            self.pbc_comp_bitmap = bytearray(4096)
            self.bitmap_size = 0x8000

        self.pbc_comp_payload = 0
        #self.sec_rev = 1

        # fill in the calculated data
        self.hash_and_map()

        self.i2c_rules = []
        for i in self.manifest['i2c-rules']:
            # the json should have in the order- bus-id, rule-id, address, size and cmd-whitelist
            self.i2c_rules.append((i['bus-id'], i['rule-id'], i['address'], i['cmd-whitelist']))

        # I2C rules PFM array
        self.pfm_i2c_rules = []

        # Generate the i2c rules
        self.build_i2c_rules()

        # Generate PFM region binary - pfm.bin
        self.build_pfm()
        print("PFM build done")

        # Generate PBC region - pbc.bin
        self.pbc_hdr()
        print("PBC build done")

    def hash_compress_regions(self, p, upd):

        # JSON format as below
        # 0. "name": <image region name>
        # 1. "index": 1,
        # 2. "offset": <start addr>,
        # 3. "size": <size of the region>,
        # 4. "prot_mask": <PFR protection mask>,
        # 5. "pfm": <1|0 -add in PFM or not>,
        # 6. "hash": <hashing of the region needed>,
        # 7. "compress": <region to be compressed>

        image_name = p[0]
        start_addr = int(p[2],16) #image part start address
        size = int(p[3],16)       #size of the image part
        pfm_prot_mask = p[4]      # pfm protection mask
        pfm_flag = p[5]           # pfm needed?
        hash_flag = p[6]          #to be hashed?
        compress = p[7]           #compress flag
        index = p[1]              # image part index
        # 1 page is 4KB
        page = start_addr >> 12

        # find skip ranges in page # in 'pfm' and 'rc-image' area
        # The pages to be skipped for HASH and PBC
        # Pages: 0x80 to 0x9f - starting PFM region until end of pfm
        # Pages: 0x2a00 to 0x7FFF - starting RC-image until end of flash
        # in reference design: EXCLUDE_PAGES =[[0x80, 0x9f],[0x2a00,0x7fff]]
        '''
        for i in self.manifest['image-parts']:
            if i['name']=='pfm':
                idx = i['index']
                pfm_st = int(i['offset'], 16)
                pfm_end= int(i['offset'], 16) + int(i['size'], 16)
            if i['name']=='rc-image':
                idx = i['index']
                rcimg_st = int(i['offset'], 16)
                rcimg_end= int(i['offset'], 16) + int(i['size'], 16)

        exclude_pages =[[pfm_st//0x1000, (pfm_end-0x1000)//0x1000],[rcimg_st//0x1000, (rcimg_end-0x1000)//0x1000]]
        '''

        print("exclude_pages = ", self.manifest["exclude-pages"])

        with open(self.firmware_file, "rb") as f:
            f.seek(start_addr)
            skip = False

            if hash_flag == 1:
                if self.sha == "1":
                    hash_dgst = hashlib.sha256()
                if self.sha == "2":
                    hash_dgst = hashlib.sha384()
            for chunk in iter(lambda: f.read(self.page_size), b''):
                chunk_len = len(chunk)
                if chunk_len != self.page_size:
                    chunk = b''.join([chunk, b'\xff' * (self.page_size - chunk_len)])

                for p in self.manifest["exclude-pages"]:
                    if (page >= p[0]) and (page <= p[1]):
                        #print("Exclude page={}".format(page))
                        skip = True
                        break

                if not skip:
                    # add to the hash
                    if hash_flag == 1:
                        # HASH for the region
                        self.act_dgst.update(chunk)
                        hash_dgst.update(chunk)

                    if compress == 1:
                        self.pbc_erase_bitmap[page >> 3] |= 1 << (7- (page % 8)) # Big endian bit map
                        # add to the pbc map
                        if chunk != self.empty:
                            #print("compressed page ={}".format(page))
                            upd.write(chunk)
                            self.pbc_comp_bitmap[page >> 3] |= 1 << (7- (page % 8)) # Big Endian bit map
                            self.pbc_comp_payload += chunk_len # compressed payload bytes

                page += 1

                if (page * self.page_size) >= (size + start_addr):
                    break

        if pfm_flag == 1:
           self.pfm_bytes += PFM_SPI_SIZE_DEF

           hash = bytearray(self.pfm_spi_size_hash)
           hash_pres = 0

           if hash_flag == 1:
               # region's hash
               hash = hash_dgst.hexdigest()
               hash_pres = self.sha_version
               self.pfm_bytes += self.pfm_spi_size_hash
           # append to SPI regions in PFM
           self.pfm_spi_regions.append(pfm_spi(pfm_prot_mask, start_addr, (start_addr+size), hash, hash_pres))

    def add_i2c_rules(self, i):
        bus_id = i[0]  # I2C Bus number
        rule_id = i[1] # I2C rule number
        addr = i[2]    # I2C device address
        cmds = i[3]    # I2C white listed commands for which i2c write to be allowed
        whitelist_map = bytearray(32)

        self.pfm_bytes += PFM_I2C_SIZE # add upto PFM size

        for c in cmds:
            if c == "all":
                for i in range(32):
                    whitelist_map[i] = 0xff
                break
            else:
                idx = int(c,16) // 8 # index in the 32 bytes of white list i2c cmds
                bit = int(c,16) % 8 # bit position to set
                whitelist_map[idx] |= (1 << bit)

        # append to I2C rules in PFM
        self.pfm_i2c_rules.append(pfm_i2c(bus_id, rule_id, addr, whitelist_map))

    def build_i2c_rules(self):
        for i in self.i2c_rules:
            self.add_i2c_rules(i)

    def hash_and_map(self):

        # have copy of the update file for appending with PFR meta and compression
        copyfile(self.firmware_file, self.pfr_rom_file)
        with open("%s-bmc_compressed.bin" % self.platform_name, "wb+") as upd:
            for p in self.image_parts:
                #filename, index, offset, size, protection.
                print(p[0], p[1], p[2], p[3], p[4])
                self.hash_compress_regions(p, upd)

    def pbc_hdr(self):
        '''
        typedef struct {
            uint8_t  tag[4];             /* PBC tag */
            uint32_t version;            /* PBC Version- 0x0000_0002 */
            uint32_t page_size;          /* NOR Flash page size = 0x0000_1000 */
            uint32_t pattern_size;       /* 0xFF as pattern 1byte = 0x0000_0001 */
            uint32_t pattern;            /* 0xFF pattern = 0x0000_00FF */
            uint32_t bitmap_size;        /* 32768 pages for 128MB- 0x0000_8000 */
            uint32_t payload_length      /* payload */
            uint8_t  reserved[100];      /* Reserved 100bytes */
            uint8_t  erase_bitmap[4096]; /* erase bit map for 32768 pages */
            uint8_t  comp_bitmap[4096];  /* compression bit map for 32768 pages */
            uint8_t  comp_payload;       /* compressed payload */
        '''
        names = [
            'tag', 'pbc_ver', 'page_sz', 'pattern_sz', 'pattern', 'bitmap_sz',
            'payload_size', 'resvd0', 'erase_bitmap', 'comp_bitmap',
            ]
        parts = {
            'tag': b'CBP_',
            'pbc_ver': struct.pack('<i',0x00000002),
            'page_sz': struct.pack('<i',0x00001000),
            'pattern_sz': struct.pack('<i',0x00000001),
            'pattern': struct.pack('<i',0x000000FF),
            'bitmap_sz': struct.pack('<i',self.bitmap_size),
            'payload_size': struct.pack('<i',self.pbc_comp_payload),
            'resvd0' : b'\x00'*100,
            'erase_bitmap': bytes(self.pbc_erase_bitmap),
            'comp_bitmap': bytes(self.pbc_comp_bitmap),
            }

        with open("%s-pbc.bin" % self.platform_name, "wb+") as pbf:
            pbf.write(b''.join([parts[n] for n in names]))
            pbf.seek(0) # rewind to beginning of PBC file
            self.act_dgst.update(pbf.read()) # add up PBC data for hashing

    def build_pfm(self):
        """ build PFM following PFR structure """
        names = [
            'tag', 'sec_rev', 'bkc', 'pfm_ver_major', 'pfm_ver_minor', 'resvd0', 'build_num', 'resvd1', 'pfm_len',
            ]
        #print("self.build_number: %s, self.build_version: %s"%(self.build_number, self.build_version))
        parts = {
            'tag': struct.pack("<I", 0x02b3ce1d),
            'sec_rev': struct.pack('<B', int(self.sec_rev, 0)),
            'bkc': struct.pack('<B', int(self.bkc_ver, 0)),
            'pfm_ver_major': struct.pack('<B', int(self.build_ver_maj, 0)),
            'pfm_ver_minor': struct.pack('<B', int(self.build_ver_min, 0)),
            'resvd0': b'\xff'* 4,
            'build_num': struct.pack('<I', int(self.build_number, 0)),
            'resvd1': b'\xff'* 12,
            'pfm_len': ''
            }

        # PFM should be 128bytes aligned, find the padding bytes
        padding_bytes = 0
        if (self.pfm_bytes % 128) != 0:
            padding_bytes = 128 - (self.pfm_bytes % 128)

        print("padding={}".format(padding_bytes))
        print("PFM size1={}".format(self.pfm_bytes))
        self.pfm_bytes += padding_bytes
        parts['pfm_len'] = struct.pack('<I', self.pfm_bytes)
        print("PFM size2={}".format(self.pfm_bytes))

        with open("%s-pfm.bin" % self.platform_name, "wb+") as f:
            f.write(b''.join([parts[n] for n in names]))
            for i in self.pfm_spi_regions:
                f.write(struct.pack('<B', int(i.spi_pfm)))
                f.write(struct.pack('<B', int(i.spi_prot_mask)))
                f.write(struct.pack('<h', int(i.spi_hash_pres)))
                f.write(struct.pack('<I', int(i.spi_pfm_rsvd)))
                f.write(struct.pack('<I', int(i.spi_start_addr)))
                f.write(struct.pack('<I', int(i.spi_end_addr)))

                if i.spi_hash_pres != 0:
                    f.write(bytearray.fromhex(i.spi_hash))

            for r in self.pfm_i2c_rules:
                f.write(struct.pack('<B', int(r.i2c_pfm)))
                f.write(struct.pack('<I', int(r.i2c_pfm_rsvd)))
                f.write(struct.pack('<B', int(r.i2c_bus_id)))
                f.write(struct.pack('<B', int(r.i2c_rule_id)))
                f.write(struct.pack('<B', int(r.i2c_address, 16)))
                f.write(r.i2c_cmd_whitelist)

            # write the padding bytes at the end
            f.write(b'\xff' * padding_bytes)

def main(args):
    parser = argparse.ArgumentParser(description='build bmc pfr image, pfm and capsule, from image manifest json file.')
    parser.add_argument('-p', '--platform',         metavar="[platform]",     dest='platform',       default=None,   help='platform name')
    parser.add_argument('-m', '--manifest',         metavar="[manifest json file]", dest='manifest', default=None,   help='manifest json file name')
    parser.add_argument('-i', '--image',            metavar="[platform_active_image]",   dest='image',   default=None,   help='platform_active_image'  )
    parser.add_argument('-j', '--build_ver_maj',    metavar="[build ver max]",  dest='build_ver_maj', default=None,   help='build major version-offset 06')
    parser.add_argument('-n', '--build_ver_min',    metavar="[build ver min]",  dest='build_ver_min', default=None,   help='build minor version-offset 07')
    parser.add_argument('-b', '--build_num',        metavar="[build number]", dest='build_num', default=None,   help='build number tracked in oem bytes, 4 bytes')
    parser.add_argument('-s', '--svn',              metavar="[svn]", dest='svn', default=None,   help='SVN in PFM, 0x0-0x40, greater than 64 is invalid')
    parser.add_argument('-v', '--bkc_ver',          metavar="[BKC version]", dest='bkc_ver', default=None,   help='bkc version')
    parser.add_argument('-a', '--sha_algo',         metavar="[sha algorithm]", dest='sha_algo', default=None,   help='sha algorithm')
    parser.add_argument('-o', '--output_file_name', metavar="[output file name]", dest='output_filename', default=None,   help='output file name')
    args = parser.parse_args(args)
    print(args)
    pfr_bmc_image(args.platform, args.manifest, args.image, args.build_ver_maj, args.build_ver_min, args.build_num, args.svn, args.bkc_ver, args.sha_algo, args.output_filename)

if __name__ == '__main__':
  main(sys.argv[1:])
