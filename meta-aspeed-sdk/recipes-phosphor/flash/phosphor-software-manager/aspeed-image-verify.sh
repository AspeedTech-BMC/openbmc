#!/bin/bash
# AST27x0 A2 verifies BMC/u-boot image vendor key hash against OTP before flashing
set -euo pipefail

VERSION_ID="$1"
IMG_UPLOAD_DIR="${IMG_UPLOAD_DIR:-/tmp/images}"
IMG_DIR="${IMG_UPLOAD_DIR}/${VERSION_ID}"
OTP_VENDOR_KEY="/sys/bus/platform/devices/14c07000.otp/vendor_key_hash"

verify_image() {
    local image="$1"
    local vendor_key_len=1920

    # Flash Header layout:
    #   0x00 ( 0): magic "FLSH" + flags       8 bytes
    #   0x08 ( 8): Checksum Block              8 bytes
    #   0x10 (16): image[0] type               4 bytes
    #   0x14 (20): image[0] offset  <-- here   4 bytes  (caliptra image start)
    # skip=20 reads image[0].offset, then +8 skips the caliptra header to reach
    # the vendor key table (1920 bytes) for sha384 hash comparison against OTP
    local off
    off=$(hexdump -s 20 -n 4 -e '1/4 "%u\n"' "${image}")

    local vendor_key_off=$((off + 8))
    local image_size
    image_size=$(stat -c '%s' "${image}")
    if [ $((vendor_key_off + vendor_key_len)) -gt "${image_size}" ]; then
        echo "aspeed-image-verify: ${image} offset 0x$(printf '%x' ${vendor_key_off}) + ${vendor_key_len} exceeds image size ${image_size}" >&2
        return 1
    fi
    local image_hash
    image_hash=$(dd if="${image}" bs=1 skip="${vendor_key_off}" count="${vendor_key_len}" 2>/dev/null \
        | openssl dgst -sha384 | awk '{print $NF}')

    local otp_hash
    otp_hash=$(tr -d '[:space:]' < "${OTP_VENDOR_KEY}")

    if [ "${image_hash}" = "${otp_hash}" ]; then
        echo "aspeed-image-verify: ${image} passed"
        return 0
    else
        echo "aspeed-image-verify: ${image} failed" >&2
        echo "  image hash: ${image_hash}" >&2
        echo "  otp hash:   ${otp_hash}" >&2
        return 1
    fi
}

if [ ! -r "${OTP_VENDOR_KEY}" ]; then
    echo "aspeed-image-verify: OTP vendor key not found, skip"
    exit 0
fi

# OTP not programmed (all zeros) — skip verification
otp_hash=$(tr -d '[:space:]' < "${OTP_VENDOR_KEY}")
if [[ "${otp_hash}" =~ ^0+$ ]]; then
    echo "aspeed-image-verify: OTP vendor key not programmed, skip"
    exit 0
fi

found=0
for img in image-bmc image-u-boot; do
    if [ -f "${IMG_DIR}/${img}" ]; then
        found=1
        verify_image "${IMG_DIR}/${img}"
    fi
done

if [ "${found}" -eq 0 ]; then
    echo "aspeed-image-verify: no image found in ${IMG_DIR}, skip"
    exit 0
fi
