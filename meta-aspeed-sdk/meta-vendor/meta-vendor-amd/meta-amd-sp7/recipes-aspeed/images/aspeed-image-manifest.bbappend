# AST2700 mcu-runtime bundle build.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

A2_ZEPHYR_IMAGE ?= "zephyr-mcu-runtime-a2.bin"
A1_ZEPHYR_IMAGE ?= "${BOOTMCU_FW_BINARY}"
OUT_A1_ZEPHYR_IMAGE ?= "ast-zephyr-mcu-runtime-a1.bin"
AST_HEADER_BIN  ?= "ast_default_header.bin"
CALIPTRA_MANIFEST_CONFIG ?= "force_caliptra_file_end.toml"

SRC_URI += "file://${CALIPTRA_MANIFEST_CONFIG}"
SRC_URI += "file://${AST_HEADER_BIN}"
SRC_URI += "file://${A2_ZEPHYR_IMAGE}"

do_compile() {
    bbnote "Running AST2700 mcu-runtime bundle image build"

    # Add ASPEED FMC header to A1 zephyr image.
    dd if=${UNPACKDIR}/${AST_HEADER_BIN} of=${B}/${OUT_A1_ZEPHYR_IMAGE}  bs=1 seek=0
    dd if=${DEPLOY_DIR_IMAGE}/${A1_ZEPHYR_IMAGE} of=${B}/${OUT_A1_ZEPHYR_IMAGE}  bs=1 seek=2560 #0xA00

    # Copy A2 zephyr image to deploy folder.
    cp ${UNPACKDIR}/${A2_ZEPHYR_IMAGE} ${DEPLOY_DIR_IMAGE}

    # Bundle image.
    cptra-imgtool \
        create-auth-flash \
        --cfg ${UNPACKDIR}/${CALIPTRA_MANIFEST_CONFIG} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        --man ${B}/${OUT_A1_ZEPHYR_IMAGE} \
        --flash ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE}

    # Use OUT_A1_ZEPHYR_IMAGE as CALIPTRA_MANIFEST_SOC_IMAGE
    # to avoid build fail at aspeed-image-recoveryuart.
    cp ${B}/${OUT_A1_ZEPHYR_IMAGE} ${B}/${CALIPTRA_MANIFEST_SOC_IMAGE}
}
