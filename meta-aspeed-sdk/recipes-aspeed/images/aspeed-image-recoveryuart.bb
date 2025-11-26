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
# AST2600 SOURCE_IMAGE = u-boot-spl.bin
# AST2700 A1 SOURCE_IMAGE = ast2700-caliptra-fw.bin
SOURCE_IMAGE:aspeed-g7 ?= "${CALIPTRA_FW_BINARY}"
SOURCE_IMAGE ?= "u-boot-spl.bin"

RECOVERY_SOURCE_IMAGE ?= "recovery_source.bin"
RECOVERY_OUTPUT_IMAGE ?=  "recovery_${SOURCE_IMAGE}"

OUTPUT_IMAGE_DIR ?= "${S}/output"
SOURCE_IMAGE_DIR ?= "${S}/source"

do_deploy () {
    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        if [ -z ${CALIPTRA_FW_BINARY} ]; then
            bbfatal "Boot from UART mode only support caliptra firmware"
        fi
    elif [ "${SOC_FAMILY}" = "aspeed-g6" ] ; then
        if [ -z ${SPL_BINARY} ]; then
            bbfatal "Boot from UART mode only support SPL"
        fi
    else
        bbfatal "Unsupport Machine"
    fi

    rm -rf ${SOURCE_IMAGE_DIR}
    rm -rf ${OUTPUT_IMAGE_DIR}
    install -d ${SOURCE_IMAGE_DIR}
    install -d ${OUTPUT_IMAGE_DIR}

    # Install recovery source into the source directory and generate the source image.
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${SOURCE_IMAGE} ${SOURCE_IMAGE_DIR}

    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${SOURCE_IMAGE} ${SOURCE_IMAGE_DIR}/${RECOVERY_SOURCE_IMAGE}

        # Generate AST2700 A1 SPL recovery image
        install -m 0644 ${DEPLOY_DIR_IMAGE}/${BOOTMCU_FW_BINARY} ${SOURCE_IMAGE_DIR}/.
        python3 ${STAGING_BINDIR_NATIVE}/recovery_spl_extraction.py -i ${SOURCE_IMAGE_DIR}/${BOOTMCU_FW_BINARY}
        install -m 0644 ${SOURCE_IMAGE_DIR}/recovery_${BOOTMCU_FW_BINARY} ${OUTPUT_IMAGE_DIR}/.

        # Deploy AST2700 A1 SPL recovery image
        install -d ${DEPLOYDIR}
        install -m 644 ${OUTPUT_IMAGE_DIR}/recovery_${BOOTMCU_FW_BINARY} ${DEPLOYDIR}/.

        # Generate the AST2700 A1 I2C/I3C recovery image.
        # If UBOOT_FITIMAGE_ENABLE is enabled, it indicates that the build uses a U-Boot FIT image
        # instead of a SoC manifest image. In this case, generate and deploy the U-Boot FIT header.
        if [ "${UBOOT_FITIMAGE_ENABLE}" = "1" ]; then
            install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot.bin ${SOURCE_IMAGE_DIR}/.
            dd if=${SOURCE_IMAGE_DIR}/u-boot.bin of=${OUTPUT_IMAGE_DIR}/u-boot-fit-header.bin bs=64 count=1
            install -m 644 ${OUTPUT_IMAGE_DIR}/u-boot-fit-header.bin ${DEPLOYDIR}/.
        fi
    elif [ "${SOC_FAMILY}" = "aspeed-g6" ] ; then
        install -m 0644 ${SOURCE_IMAGE_DIR}/${SOURCE_IMAGE} ${SOURCE_IMAGE_DIR}/${RECOVERY_SOURCE_IMAGE}
    fi

    # Generate recovery image via UART
    python3 ${STAGING_BINDIR_NATIVE}/gen_uart_booting_image.py ${SOURCE_IMAGE_DIR}/${RECOVERY_SOURCE_IMAGE} ${OUTPUT_IMAGE_DIR}/${RECOVERY_OUTPUT_IMAGE}

    # Deploy recovery image via UART
    install -d ${DEPLOYDIR}
    install -m 644 ${OUTPUT_IMAGE_DIR}/${RECOVERY_OUTPUT_IMAGE} ${DEPLOYDIR}/.
}

do_deploy[depends] += " \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-bootmcu', 'virtual/bootmcu:do_deploy', '', d)} \
    "

addtask deploy before do_build after do_compile
