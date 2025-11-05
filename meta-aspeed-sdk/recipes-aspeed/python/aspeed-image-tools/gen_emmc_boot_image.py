#!/usr/bin/env python3

import sys
import struct


def insert_bytearray(src, dst, offset):
    if offset+src.__len__() > dst.__len__():
        dst.extend(bytearray(offset-dst.__len__()+src.__len__()))

    dst[offset:offset+src.__len__()] = src


with open(sys.argv[1], 'rb') as f:
    image = bytearray(f.read())
    f.close()

image_len = (image.__len__() + 511) & (~511)

if image_len > 64*1024:
    raise ValueError('The maximum size of image is 64 KBytes.')

len_bin = struct.pack('<I', image_len)


insert_bytearray(len_bin, image, 0x28)
image.extend(bytearray(image_len - image.__len__()))

with open(sys.argv[2], 'w+b') as f:
    f.write(bytes(image))
    f.close()
