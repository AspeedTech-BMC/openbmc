inherit python3native setuptools3

DEPENDS:append = " cerberus-pfr-signing-utility-native \
                   ${PYTHON_PN}-pycryptodome-native \
                 "

IMAGE_INSTALL:append = " packagegroup-aspeed-pfr-apps \
                         packagegroup-cerberus-pfr-apps \
                       "

do_generate_static:append() {
    bb.build.exec_func("do_generate_signed_pfr_image", d)
}

# PFR images directory
PFR_DEPLOY_IMAGES_DIR = "${IMGDEPLOYDIR}/pfr_images"
PFR_IMAGES_DIR = "${S}/pfr_images"
PFR_IMAGE_BIN = "image-mtd-pfr"

# PFR image generation script directory
PFR_MANIFEST_TOOLS_DIR = "${STAGING_DIR_NATIVE}${datadir}/cerberus/manifest_tools"
PFR_RECOVERY_TOOLS_DIR = "${STAGING_DIR_NATIVE}${datadir}/cerberus/recovery_tools"

do_generate_signed_pfr_image(){
    if [ -d ${PFR_IMAGES_DIR} ]; then
        rm -rf ${PFR_IMAGES_DIR}
    fi

    install -d ${PFR_IMAGES_DIR}

    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        OBMC_PFM_CONFIG="obmc_pfm_generator_2700.config"
        OBMC_RECOVERY_IMAGE_CONFIG="obmc_recovery_image_generator_2700.config"
    else
        OBMC_PFM_CONFIG="obmc_pfm_generator.config"
        OBMC_RECOVERY_IMAGE_CONFIG="obmc_recovery_image_generator.config"
    fi

    # Assemble the flash image
    mk_empty_image ${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN} ${PFR_IMAGE_SIZE}

    dd bs=1k conv=notrunc seek=0 \
        if=${IMGDEPLOYDIR}/${IMAGE_NAME}.static.mtd \
        of=${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN}

    # create PFM manifest
    rm -f ${PFR_MANIFEST_TOOLS_DIR}/${PFR_IMAGE_BIN}
    rm -f ${PFR_MANIFEST_TOOLS_DIR}/obmc_pfm.bin
    install ${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN} ${PFR_MANIFEST_TOOLS_DIR}
    cd ${PFR_MANIFEST_TOOLS_DIR}
    python3 pfm_generator.py ${OBMC_PFM_CONFIG}
    install obmc_pfm.bin ${PFR_IMAGES_DIR}/.
    cd ${S}

    # add the signed PFM to rom image
    dd bs=1k conv=notrunc seek=${PFM_OFFSET_PAGE} \
        if=${PFR_IMAGES_DIR}/obmc_pfm.bin \
        of=${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN}

    # create recovery image
    rm -f ${PFR_RECOVERY_TOOLS_DIR}/${PFR_IMAGE_BIN}
    rm -f ${PFR_RECOVERY_TOOLS_DIR}/obmc_recovery_image.bin
    install ${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN} ${PFR_RECOVERY_TOOLS_DIR}
    cd ${PFR_RECOVERY_TOOLS_DIR}
    python3 recovery_image_generator.py ${OBMC_RECOVERY_IMAGE_CONFIG}
    install obmc_recovery_image.bin ${PFR_IMAGES_DIR}/.
    cd ${S}

    # add the signed recovery to rom image
    dd bs=1k conv=notrunc seek=${RC_IMAGE_PAGE} \
        if=${PFR_IMAGES_DIR}/obmc_recovery_image.bin \
        of=${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN}

    # deploy pfr images
    if [ -d ${PFR_DEPLOY_IMAGES_DIR} ]; then
        rm -rf ${PFR_DEPLOY_IMAGES_DIR}
    fi

    # Add "bmc_signed_cap.bin" for PFR BMC firmware update.
    ln -sf ${PFR_IMAGES_DIR}/obmc_recovery_image.bin  ${PFR_IMAGES_DIR}/bmc_signed_cap.bin

    install -d ${PFR_DEPLOY_IMAGES_DIR}
    install -m 0644 ${PFR_IMAGES_DIR}/*.bin ${PFR_DEPLOY_IMAGES_DIR}/.
    install -m 0644 ${PFR_IMAGES_DIR}/${PFR_IMAGE_BIN} ${PFR_DEPLOY_IMAGES_DIR}/.
    # check dual flash setting
    if [ "${DUAL_FLASH}" = "1" ]; then
        [ -z "${FIRST_FLASH_SIZE}" ] && bberror "FIRST_FLASH_SIZE not defined"
        dd if=${PFR_DEPLOY_IMAGES_DIR}/${PFR_IMAGE_BIN} of=${PFR_DEPLOY_IMAGES_DIR}/${PFR_IMAGE_BIN}.1 bs=1K count=${FIRST_FLASH_SIZE}
        dd if=${PFR_DEPLOY_IMAGES_DIR}/${PFR_IMAGE_BIN} of=${PFR_DEPLOY_IMAGES_DIR}/${PFR_IMAGE_BIN}.2 bs=1K skip=${FIRST_FLASH_SIZE}
    fi

}

