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
# AST2700 A0 SOURCE_IMAGE = u-boot-spl.bin (ast2700a0-ibex-spl.bin)
# AST2700 A1 SOURCE_IMAGE = ast2700-caliptra-fw.bin
SOURCE_IMAGE:aspeed-g7 ?= "${@bb.utils.contains('MACHINE_FEATURES', 'ast2700-a0', 'u-boot-spl.bin', 'ast2700-caliptra-fw.bin', d)}"
SOURCE_IMAGE ?= "u-boot-spl.bin"

RECOVERY_SOURCE_IMAGE ?= "recovery_source.bin"
RECOVERY_OUTPUT_IMAGE ?=  "recovery_${SOURCE_IMAGE}"

OUTPUT_IMAGE_DIR ?= "${S}/output"
SOURCE_IMAGE_DIR ?= "${S}/source"

IS_AST2700_A0 ?= "${@bb.utils.contains('MACHINE_FEATURES', 'ast2700-a0', 'yes', 'no', d)}"

do_deploy () {
    if [ "${SOC_FAMILY}" = "aspeed-g7" ]; then
        if [ "${IS_AST2700_A0}" = "yes" ]; then
            if [ -z ${BOOTMCU_FW_BINARY} ]; then
                bbfatal "Boot from UART mode only support BootMCU SPL"
            fi
        else
            if [ -z ${CALIPTRA_FW_BINARY} ]; then
                bbfatal "Boot from UART mode only support caliptra firmware"
            fi
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
        if [ "${IS_AST2700_A0}" = "yes" ]; then
            dd bs=1 count=128 if=/dev/zero  | tr '\000' '\377' > ${SOURCE_IMAGE_DIR}/${RECOVERY_SOURCE_IMAGE}
            dd bs=1 seek=128 conv=notrunc if=${SOURCE_IMAGE_DIR}/${SOURCE_IMAGE} of=${SOURCE_IMAGE_DIR}/${RECOVERY_SOURCE_IMAGE}
        else
            install -m 0644 ${DEPLOY_DIR_IMAGE}/${SOURCE_IMAGE} ${SOURCE_IMAGE_DIR}/${RECOVERY_SOURCE_IMAGE}

            # Generate AST2700 A1 SPL recovery image
            install -m 0644 ${DEPLOY_DIR_IMAGE}/${BOOTMCU_FW_BINARY} ${SOURCE_IMAGE_DIR}/.
            python3 ${STAGING_BINDIR_NATIVE}/recovery_spl_extraction.py -i ${SOURCE_IMAGE_DIR}/${BOOTMCU_FW_BINARY}
            install -m 0644 ${SOURCE_IMAGE_DIR}/recovery_${BOOTMCU_FW_BINARY} ${OUTPUT_IMAGE_DIR}/.

            # Generate AST2700 A1 I2C/I3C recovery image
            install -m 0644 ${DEPLOY_DIR_IMAGE}/u-boot.bin ${SOURCE_IMAGE_DIR}/.
            dd if=${SOURCE_IMAGE_DIR}/u-boot.bin of=${OUTPUT_IMAGE_DIR}/u-boot-fit-header.bin bs=64 count=1

            # Deploy AST2700 A1 SPL recovery image
            install -d ${DEPLOYDIR}
            install -m 644 ${OUTPUT_IMAGE_DIR}/recovery_${BOOTMCU_FW_BINARY} ${DEPLOYDIR}/.

            # Deploy AST2700 A1 I2C/I3C recovery image
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
    ${@bb.utils.contains('MACHINE_FEATURES', 'ast-caliptra', 'caliptra:do_deploy', '', d)} \
    "

addtask deploy before do_build after do_compile
