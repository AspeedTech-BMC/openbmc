DESCRIPTION = "Generate recovery image via UART for ASPEED BMC SoCs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${ASPEEDSDKBASE}/LICENSE;md5=a3740bd0a194cd6dcafdc482a200a56f"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

DEPENDS = "aspeed-image-tools-native"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"

inherit deploy

# Image composition
#
# AST2600 source images:
# - SPL (u-boot-spl.bin)
#
# AST2700 A1 source images:
# - Caliptra firmware image (${CALIPTRA_FW_BINARY})
# - SoC First Mutable Code image (${BOOTMCU_FW_BINARY})
#
# AST2700 A2 source images:
# - Caliptra firmware image (${CALIPTRA_FW_BINARY})
# - Caliptra SoC manifest image (${CALIPTRA_MANIFEST_SOC_IMAGE})
# - MCU runtime firmware binary (zephyr-aspeed-bootmcu.bin)

SOURCE_IMAGES ?= "${CALIPTRA_FW_BINARY} ${CALIPTRA_MANIFEST_SOC_IMAGE} zephyr-aspeed-bootmcu.bin"
SOURCE_IMAGES:ast2700-a1 ?= "${CALIPTRA_FW_BINARY}"
SOURCE_IMAGES:aspeed-g6 ?= "u-boot-spl.bin"

OUTPUT_IMAGE_DIR ?= "${S}/output"
SOURCE_IMAGE_DIR ?= "${S}/source"

do_deploy () {
    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        if [ -z "${SOURCE_IMAGES}" ]; then
            bbfatal "No source images specified for Boot from-UART mode"
        fi
    elif [ "${SOC_FAMILY}" = "aspeed-g6" ] ; then
        if [ -z "${SPL_BINARY}" ]; then
            bbfatal "Boot from UART mode only support SPL"
        fi
    else
        bbfatal "Unsupport Machine"
    fi

    rm -rf ${SOURCE_IMAGE_DIR}
    rm -rf ${OUTPUT_IMAGE_DIR}
    install -d ${SOURCE_IMAGE_DIR}
    install -d ${OUTPUT_IMAGE_DIR}

    # Install all source images into the staging source directory
    for source_image in ${SOURCE_IMAGES}; do
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${source_image} ${SOURCE_IMAGE_DIR}
    done

    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        # Generate the SoC First Mutable Code (FMC) recovery image
        # This step is applicable only to AST2700 A1
        if [ "${FMC_IMAGE_ENABLE}" = "1" ]; then
            install -m 0644 ${DEPLOY_DIR_IMAGE}/${BOOTMCU_FW_BINARY} ${SOURCE_IMAGE_DIR}/.
            python3 ${STAGING_BINDIR_NATIVE}/recovery_spl_extraction.py -i ${SOURCE_IMAGE_DIR}/${BOOTMCU_FW_BINARY}
            install -m 0644 ${SOURCE_IMAGE_DIR}/recovery_${BOOTMCU_FW_BINARY} ${OUTPUT_IMAGE_DIR}/.
        fi

        # Generate the I2C/I3C recovery image for AST2700 A1
        #
        # When UBOOT_FITIMAGE_ENABLE is set to "1", the build uses a U-Boot FIT
        # image instead of a Caliptra Manifest Flash image. In this case, extract
        # and generate the U-Boot FIT header for recovery usage.
        if [ "${UBOOT_FITIMAGE_ENABLE}" = "1" ]; then
            install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot.bin ${SOURCE_IMAGE_DIR}/.
            dd if=${SOURCE_IMAGE_DIR}/u-boot.bin of=${OUTPUT_IMAGE_DIR}/u-boot-fit-header.bin bs=64 count=1
        fi
    fi

    # Generate UART recovery images from all source images
    for source_image in ${SOURCE_IMAGES}; do
        output_image="recovery_${source_image}"
        python3 ${STAGING_BINDIR_NATIVE}/gen_uart_booting_image.py \
            ${SOURCE_IMAGE_DIR}/${source_image} \
            ${OUTPUT_IMAGE_DIR}/${output_image}
    done

    # Deploy all generated UART recovery images
    install -d ${DEPLOYDIR}
    install -m 644 ${OUTPUT_IMAGE_DIR}/* ${DEPLOYDIR}/.
}

do_deploy[depends] += " \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-bootmcu', 'virtual/bootmcu:do_deploy', '', d)} \
    ${@oe.utils.conditional('SOC_FAMILY', 'aspeed-g7', \
        oe.utils.conditional('UBOOT_FITIMAGE_ENABLE', '1', '', 'aspeed-image-manifest:do_deploy', d), \
        '', d)} \
    "

addtask deploy before do_build after do_compile
