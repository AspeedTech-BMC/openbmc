# According to the design of AST2700, bootmcu(riscv-32) execute SPL and CPU(coretax-a35) execute u-boot.
# We added the do_merge_uboot task to merge the bootmcu and u-boot image before do_generate_static
# to ensure compatibility with image_types_phosphor.bbclass.
UBOOT_BINARY := "u-boot.${UBOOT_SUFFIX}"
UBOOT_BINARY:ast-irot := "${IROT_IMAGE}"
UBOOT_SUFFIX:append = ".merged"

# Install the image-u-boot to deploy folder when building the emmc image.
do_generate_ext4_tar:append() {
    cd ${S}/ext4
    install -m 644 image-u-boot ${IMGDEPLOYDIR}/image-u-boot
}

# Merge Caliptra, bootmcu and u-boot image
do_merge_uboot() {
    uboot_offset=0

    # Check for Caliptra. If it exists, merge the Caliptra image with the U-Boot image.
    if [ ! -z ${CALIPTRA_FW_BINARY} ]; then
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
    else
        mk_empty_image_zeros ${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX} ${FLASH_BMCU_SIZE}
    fi

    # Check bootmcu size
    imgpath=${DEPLOY_DIR_IMAGE}/${BOOTMCU_FW_BINARY}
    imgsize=$(wc -c < "$imgpath")
    maxsize=$(expr ${FLASH_BMCU_SIZE} \* 1024)

    if [ "$imgsize" -gt "$maxsize" ]; then
        echo "Error: BOOTMCU $imgpath size ($imgsize bytes) exceeds $maxsize."
        exit 1
    fi

    # Concatenate bootmcu and u-boot image
    dd bs=1k seek=${uboot_offset} if=${DEPLOY_DIR_IMAGE}/${BOOTMCU_FW_BINARY} of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}
    uboot_offset=$(expr ${uboot_offset} + ${FLASH_BMCU_SIZE})

    dd bs=1k seek=${uboot_offset} if=${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY} of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}
}

do_merge_uboot[depends] += " \
    u-boot:do_deploy \
    virtual/bootmcu:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-irot', 'aspeed-image-irot:do_deploy', '', d)} \
    "

addtask do_merge_uboot before do_generate_static after do_generate_rwfs_static

# The do_generate_ext4_tar comes from the meta-phosphor/classes/image_types_phosphor.bbclass.
# Add the EXTRA_IMAGECMD for UFS.
do_generate_ext4_tar() {
    # Generate the U-Boot image
    mk_empty_image_zeros image-u-boot ${MMC_UBOOT_SIZE}
    do_generate_image_uboot_file image-u-boot

    # Generate a compressed ext4 filesystem with the fitImage file in it to be
    # flashed to the boot partition of the eMMC
    install -d boot-image
    install -m 644 ${DEPLOY_DIR_IMAGE}/${FLASH_KERNEL_IMAGE} boot-image/fitImage
    mk_empty_image_zeros boot-image.${FLASH_EXT4_BASETYPE} ${MMC_BOOT_PARTITION_SIZE}
    mkfs.ext4 -F ${EXTRA_IMAGECMD:ext4} -d boot-image boot-image.${FLASH_EXT4_BASETYPE}
    # Error codes 0-3 indicate successfull operation of fsck
    fsck.ext4 -pvfD boot-image.${FLASH_EXT4_BASETYPE} || [ $? -le 3 ]
    zstd -f -k -T0 -c -${ZSTD_COMPRESSION_LEVEL} boot-image.${FLASH_EXT4_BASETYPE} > boot-image.${FLASH_EXT4_BASETYPE}.zst

    # Generate the compressed ext4 rootfs
    zstd -f -k -T0 -c -${ZSTD_COMPRESSION_LEVEL} ${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}.${FLASH_EXT4_BASETYPE} > ${IMAGE_LINK_NAME}.${FLASH_EXT4_BASETYPE}.zst

    ln -sf boot-image.${FLASH_EXT4_BASETYPE}.zst image-kernel
    ln -sf ${IMAGE_LINK_NAME}.${FLASH_EXT4_BASETYPE}.zst image-rofs
    ln -sf ${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}.rwfs.${FLASH_EXT4_OVERLAY_BASETYPE} image-rwfs
    ln -sf ${S}/MANIFEST MANIFEST
    ln -sf ${S}/publickey publickey

    hostfw_update_file="${DEPLOY_DIR_IMAGE}/hostfw/update/image-hostfw"
    if [ -e "${hostfw_update_file}" ]; then
        ln -sf "${hostfw_update_file}" image-hostfw
        make_signatures image-u-boot image-kernel image-rofs image-rwfs MANIFEST publickey image-hostfw
        make_tar_of_images ext4.mmc MANIFEST publickey ${signature_files} image-hostfw
    else
        make_signatures image-u-boot image-kernel image-rofs image-rwfs MANIFEST publickey
        make_tar_of_images ext4.mmc MANIFEST publickey ${signature_files}
    fi
}

do_make_ubi[depends] += "${PN}:do_merge_uboot"
do_generate_ubi_tar[depends] += "${PN}:do_merge_uboot"
do_generate_static_tar[depends] += "${PN}:do_merge_uboot"
do_generate_static_norootfs[depends] += "${PN}:do_merge_uboot"
do_generate_ext4_tar[depends] += "${PN}:do_merge_uboot"
