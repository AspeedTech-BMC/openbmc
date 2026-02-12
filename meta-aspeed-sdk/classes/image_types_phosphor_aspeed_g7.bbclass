# According to the design of AST2700, bootmcu(riscv-32) execute virtual/bootmcu and CPU(coretax-a35) execute u-boot.
# We added the do_merge_uboot task to merge the bootmcu and u-boot image before do_generate_static
# to ensure compatibility with image_types_phosphor.bbclass.
# If UBOOT_FITIMAGE_ENABLE is enabled, it means the build uses a U-Boot FIT image
# instead of a Caliptra Manifest Flash image. In this case, the U-Boot binary is u-boot.bin;
# otherwise, the U-Boot binary is the Caliptra Manifest Flash image.
UBOOT_BINARY := "${@oe.utils.conditional('UBOOT_FITIMAGE_ENABLE', '1', 'u-boot.${UBOOT_SUFFIX}', '${CALIPTRA_MANIFEST_FLASH_IMAGE}', d)}"
UBOOT_SUFFIX:append = ".merged"

# Install the image-u-boot to deploy folder when building the emmc image.
do_generate_ext4_tar:append() {
    cd ${S}/ext4
    install -m 644 image-u-boot ${IMGDEPLOYDIR}/image-u-boot
}

# Merge Caliptra, bootmcu and u-boot image
do_merge_uboot() {
    # Starting from AST2700 A2, the Caliptra Manifest Flash image includes the following components:
    # Caliptra Firmware Pre-built Image
    # MCU Runtime Binary
    # Caliptra SoC Manifest
    # ARM Trusted Firmware (ATF)
    # OPTEE OS
    # U-Boot Raw Image
    # DDR4/DDR5 Pre-built Images
    # SSP Firmware Binary and TSP Firmware Binary (optional, depending on user requirements)
    #
    # Therefore, u-boot.bin.merged, image-u-boot, and the Caliptra Manifest Flash image
    # are identical for AST2700 A2 and later.

    if [ -z "${FLASH_CALIPTRA_SIZE}" ] && [ -z "${BOOTMCU_FMC_BINARY}" ] ; then
         install -m 644 ${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY} ${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX} || { exit 1; }
         exit 0
    fi

    # The following logic applies to AST2700 A1.
    uboot_offset=0

    # Merge the Caliptra image with the U-Boot image.
    mk_empty_image_zeros ${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX} ${FLASH_CALIPTRA_SIZE}
    # Check Caliptra size
    imgpath=${DEPLOY_DIR_IMAGE}/${CALIPTRA_FW_BINARY}
    imgsize=$(wc -c < "$imgpath")
    maxsize=$(expr ${FLASH_CALIPTRA_SIZE} \* 1024)
    if [ "$imgsize" -gt "$maxsize" ]; then
        echo "Error: CALIPTRA_FW $imgpath size ($imgsize bytes) exceeds $maxsize."
        exit 1
    fi

    # Concatenate Caliptra and u-boot image
    dd bs=1k seek=0 if=${DEPLOY_DIR_IMAGE}/${CALIPTRA_FW_BINARY} of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}
    uboot_offset=${FLASH_CALIPTRA_SIZE}

    # Check bootmcu size
    imgpath=${DEPLOY_DIR_IMAGE}/${BOOTMCU_FMC_BINARY}
    imgsize=$(wc -c < "$imgpath")
    maxsize=$(expr ${FLASH_BMCU_SIZE} \* 1024)

    if [ "$imgsize" -gt "$maxsize" ]; then
        echo "Error: BOOTMCU $imgpath size ($imgsize bytes) exceeds $maxsize."
        exit 1
    fi

    # Concatenate bootmcu and u-boot image
    dd bs=1k seek=${uboot_offset} if=${DEPLOY_DIR_IMAGE}/${BOOTMCU_FMC_BINARY} of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}
    uboot_offset=$(expr ${uboot_offset} + ${FLASH_BMCU_SIZE})

    dd bs=1k seek=${uboot_offset} if=${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY} of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}
}

# If UBOOT_FITIMAGE_ENABLE is enabled, it means the build uses a U-Boot FIT image
# instead of a SoC manifest image. In this case, skip adding aspeed-image-manifest
# to the deploy task dependencies; otherwise, include it to generate the SoC manifest image.
do_merge_uboot[depends] += " \
    u-boot:do_deploy \
    virtual/bootmcu:do_deploy \
    ${@oe.utils.conditional('UBOOT_FITIMAGE_ENABLE', '1', '', 'aspeed-image-manifest:do_deploy', d)} \
    "

addtask do_merge_uboot before do_generate_static after do_generate_rwfs_static

do_make_ubi[depends] += "${PN}:do_merge_uboot"
do_generate_ubi_tar[depends] += "${PN}:do_merge_uboot"
do_generate_static_tar[depends] += "${PN}:do_merge_uboot"
do_generate_static_norootfs[depends] += "${PN}:do_merge_uboot"
do_generate_ext4_tar[depends] += "${PN}:do_merge_uboot"
