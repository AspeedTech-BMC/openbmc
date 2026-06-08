# For AST27x5, the U-Boot binary is the Caliptra Manifest Flash image, which
# already bundles Caliptra FW, SS MCU runtime, ATF, OPTEE, and U-Boot.
# do_merge_uboot also prepends the SS MCU ROM and Boot MCU ROM images at their
# respective flash offsets before the Caliptra Manifest Flash image.
UBOOT_SUFFIX:append = ".merged"

# Install the image-u-boot to deploy folder when building the emmc image.
do_generate_ext4_tar:append() {
    cd ${S}/ext4
    install -m 644 image-u-boot ${IMGDEPLOYDIR}/image-u-boot
}

do_merge_uboot() {

    mk_empty_image_zeros ${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX} ${FLASH_SSMCU_ROM_SIZE}

    # Write SS MCU ROM at offset 0
    imgpath=${DEPLOY_DIR_IMAGE}/${SSMCU_ROM_BINARY}
    imgsize=$(wc -c < "$imgpath")
    maxsize=$(expr ${FLASH_SSMCU_ROM_SIZE} \* 1024)
    if [ "$imgsize" -gt "$maxsize" ]; then
        echo "Error: SSMCU_ROM $imgpath size ($imgsize bytes) exceeds $maxsize bytes."
        exit 1
    fi
    dd bs=1k seek=0 if=${imgpath} of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}

    # Write Boot MCU ROM at offset FLASH_SSMCU_ROM_SIZE
    imgpath=${DEPLOY_DIR_IMAGE}/${BOOTMCU_ROM_BINARY}
    imgsize=$(wc -c < "$imgpath")
    maxsize=$(expr $(expr ${FLASH_ABB_OFFSET} - ${FLASH_SSMCU_ROM_SIZE}) \* 1024)
    if [ "$imgsize" -gt "$maxsize" ]; then
        echo "Error: BOOTMCU_ROM $imgpath size ($imgsize bytes) exceeds $maxsize bytes."
        exit 1
    fi
    dd bs=1k seek=${FLASH_SSMCU_ROM_SIZE} if=${imgpath} \
        of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}

    # Write Caliptra Manifest Flash image at offset FLASH_ABB_OFFSET
    dd bs=1k seek=${FLASH_ABB_OFFSET} \
        if=${DEPLOY_DIR_IMAGE}/${CALIPTRA_MANIFEST_FLASH_IMAGE} \
        of=${DEPLOY_DIR_IMAGE}/u-boot.${UBOOT_SUFFIX}
}

do_merge_uboot[depends] += " \
    u-boot:do_deploy \
    aspeed-image-manifest:do_deploy \
    bmc-pb:do_deploy \
    "

addtask do_merge_uboot before do_generate_static after do_generate_rwfs_static

do_make_ubi[depends] += "${PN}:do_merge_uboot"
do_generate_ubi_tar[depends] += "${PN}:do_merge_uboot"
do_generate_static_tar[depends] += "${PN}:do_merge_uboot"
do_generate_static_norootfs[depends] += "${PN}:do_merge_uboot"
do_generate_ext4_tar[depends] += "${PN}:do_merge_uboot"
