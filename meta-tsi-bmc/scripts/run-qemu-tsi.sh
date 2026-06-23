#!/bin/bash
#
# Launch the TSI BMC image (tsi-bmc-qemu-ast2700) on QEMU.
#
# Stage 1 reuses the ASPEED AST2700 BSP stand-in, so the boot procedure matches
# Stage 0: load U-Boot (A35), TF-A BL31 and OP-TEE into DRAM, jump the 4 A35
# cores to BL31 at 0x430000000, and expose the 64 MiB flash image as an MTD
# drive. AST2700 silicon rev A1 (Aspeed SDK v09.06) => QEMU machine
# "ast2700a1-evb".
#
# Host port forwards: SSH 2222->22, HTTPS 2443->443, IPMI RMCP+ 2623->623(udp).
# Credentials: root / 0penBmc. Quit with Ctrl-A X.
#
# Overridable via env: QEMU, IMGDIR, MACHINE, BMC_MACHINE.
#   QEMU        path to qemu-system-aarch64 (>= 9.1 for the AST2700 model)
#   BMC_MACHINE OpenBMC machine name (deploy dir), default tsi-bmc-qemu-ast2700
#   IMGDIR      deploy images dir, default derived from BMC_MACHINE
#   MACHINE     QEMU machine model, default ast2700a1-evb
set -euo pipefail

REPO="${REPO:-/scratch/smehta/openbmc}"
QEMU="${QEMU:-/scratch/smehta/qemu/build/qemu-system-aarch64}"
BMC_MACHINE="${BMC_MACHINE:-tsi-bmc-qemu-ast2700}"
IMGDIR="${IMGDIR:-${REPO}/build/${BMC_MACHINE}/tmp/deploy/images/${BMC_MACHINE}}"
MACHINE="${MACHINE:-ast2700a1-evb}"

die() { echo "error: $*" >&2; exit 1; }

[ -x "${QEMU}" ] || die "QEMU not found/executable at '${QEMU}' (set QEMU=)"
[ -d "${IMGDIR}" ] || die "deploy dir not found: '${IMGDIR}' (build tsi-obmc-phosphor-image first, or set IMGDIR=)"
for f in u-boot-nodtb.bin u-boot.dtb bl31.bin optee/tee-raw.bin image-bmc; do
    [ -e "${IMGDIR}/${f}" ] || die "missing artifact: ${IMGDIR}/${f}"
done

UBOOT_SIZE=$(stat --format=%s -L "${IMGDIR}/u-boot-nodtb.bin")

# The AST2700 EVB QEMU models (ast2700-evb / ast2700a1-evb) emulate a soldered
# w25q01jv = 128 MiB SPI-NOR on FMC CS0, and QEMU requires the MTD backing file
# to match the chip size exactly. The TSI image is 64 MiB (FLASH_SIZE=65536),
# so pad a working copy up to the chip size for QEMU only. The real 64 MiB flash
# layout is unchanged; the padded tail is empty (0xFF/0x00) and unused.
MODEL_FLASH_BYTES="${MODEL_FLASH_BYTES:-134217728}"   # 128 MiB (w25q01jv)
IMG="${IMGDIR}/image-bmc"
IMG_BYTES=$(stat --format=%s -L "${IMG}")

QIMG="${IMG}"
if [ "${IMG_BYTES}" -lt "${MODEL_FLASH_BYTES}" ]; then
    # Create/refresh a padded working copy beside the deploy image (fall back to
    # TMPDIR if the deploy dir is not writable).
    QIMG="${IMG}.qemu-${MODEL_FLASH_BYTES}"
    if ! ( : > "${QIMG}" ) 2>/dev/null; then
        QIMG="${TMPDIR:-/tmp}/$(basename "${IMG}").${BMC_MACHINE}.qemu-${MODEL_FLASH_BYTES}"
    fi
    if [ ! -s "${QIMG}" ] || [ "${IMG}" -nt "${QIMG}" ]; then
        cp -f "${IMG}" "${QIMG}"
        truncate -s "${MODEL_FLASH_BYTES}" "${QIMG}"
    fi
elif [ "${IMG_BYTES}" -gt "${MODEL_FLASH_BYTES}" ]; then
    die "image-bmc (${IMG_BYTES} B) is larger than the emulated flash (${MODEL_FLASH_BYTES} B)"
fi

echo "Launching ${BMC_MACHINE} on QEMU '${MACHINE}' from ${IMGDIR}"
echo "  MTD image: ${QIMG} ($((IMG_BYTES/1024/1024)) MiB layout in a $((MODEL_FLASH_BYTES/1024/1024)) MiB chip)"
echo "  SSH localhost:2222  HTTPS localhost:2443  IPMI udp localhost:2623"
echo "  login root / 0penBmc ; quit with Ctrl-A X"

QEMU_ARGS=( -M "${MACHINE}"
    -device loader,force-raw=on,addr=0x400000000,file="${IMGDIR}/u-boot-nodtb.bin"
    -device loader,force-raw=on,addr=$((0x400000000 + UBOOT_SIZE)),file="${IMGDIR}/u-boot.dtb"
    -device loader,force-raw=on,addr=0x430000000,file="${IMGDIR}/bl31.bin"
    -device loader,force-raw=on,addr=0x430080000,file="${IMGDIR}/optee/tee-raw.bin"
    -device loader,cpu-num=0,addr=0x430000000
    -device loader,cpu-num=1,addr=0x430000000
    -device loader,cpu-num=2,addr=0x430000000
    -device loader,cpu-num=3,addr=0x430000000
    -smp 4
    -drive file="${QIMG}",format=raw,if=mtd
    -nic user,hostfwd=tcp::2222-:22,hostfwd=tcp::2443-:443,hostfwd=udp::2623-:623
    -nographic )

# Avoid killing the caller's shell if this script was sourced (`. script`):
# only exec when executed directly.
SOURCED=0
(return 0 2>/dev/null) && SOURCED=1
if [ "${SOURCED}" -eq 1 ]; then
    "${QEMU}" "${QEMU_ARGS[@]}"
else
    exec "${QEMU}" "${QEMU_ARGS[@]}"
fi
