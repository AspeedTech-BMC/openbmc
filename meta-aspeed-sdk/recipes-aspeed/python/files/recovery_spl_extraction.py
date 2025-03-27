from __future__ import print_function
import argparse
import os, sys
sys.dont_write_bytecode = True
import re
import struct
import shutil


# Constants
HDR_SIZE = 0xA00  # Header size
BODY_SIZE_OFFSET = 449  # DWORD offset for body size
MIN_IMAGE_SIZE = 0x1000  # Minimum allowed image size
HDR_FORMAT = "<640I"  # Format string for unpacking the header

# Argument parser setup
parser = argparse.ArgumentParser()
parser.add_argument(
    '-i', '--input_image',
    help='Path to the image file for processing',
    required=True
)
args = parser.parse_args(sys.argv[1:])

# Validate input image path
if not os.path.isfile(args.input_image):
    print(f"Fail: Input image doesn't exist: {args.input_image}")
    sys.exit(1)

input_image_path = os.path.abspath(args.input_image)
input_image_name = os.path.basename(input_image_path)
input_image_size = os.path.getsize(input_image_path)
output_image_path = os.path.join(os.path.dirname(input_image_path), f"recovery_{input_image_name}")

# Process the input image
if not os.path.isfile(input_image_path):
    print(f"Fail: Input image doesn't exist: {args.input_image}")
    sys.exit(1)

if os.path.isfile(output_image_path):
    os.remove(output_image_path)

input_image = open(input_image_path, 'rb')

if input_image_size < MIN_IMAGE_SIZE:
    print(f"Fail: Image size is too small: {input_image_size} bytes")
    sys.exit(1)

hdr_data = input_image.read(HDR_SIZE)
hdr_data = struct.unpack(HDR_FORMAT, hdr_data)

print(f"Image size in header = {hex(hdr_data[BODY_SIZE_OFFSET])}")

input_image.seek(0)

output_image = open(output_image_path, 'wb')
output_image.write(input_image.read(0xa00 + hdr_data[BODY_SIZE_OFFSET]))

input_image.close()
output_image.close()

