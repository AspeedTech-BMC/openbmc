# AST2700 mcu-runtime bundle build.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

A2_ZEPHYR_IMAGE = "${BOOTMCU_FW_BINARY}"
A1_ZEPHYR_IMAGE = "zephyr-aspeed-bootmcu-a1.bin"
OUT_A1_ZEPHYR_IMAGE = "ast-zephyr-mcu-runtime-a1.bin"
AST_HEADER_BIN  = "ast_default_header.bin"
CALIPTRA_MANIFEST_CONFIG = "force_caliptra_file_end.toml"

# AST_HEADER_BIN(ast_default_header.bin) set A1 zephyr image size to 0x36000.
# Size: 0x36000 + 0xA00 (header size) = 223744 bytes
A1_ZEPHYR_IMAGE_SIZE = "223744"

SRC_URI += "file://${CALIPTRA_MANIFEST_CONFIG}"
SRC_URI += "file://${AST_HEADER_BIN}"

do_compile() {
    bbnote "Running AST2700 mcu-runtime bundle image build"

    # Create initial image filled with 0xFF bytes
    dd if=/dev/zero bs=1 count=${A1_ZEPHYR_IMAGE_SIZE} | tr '\000' '\377' > ${B}/${OUT_A1_ZEPHYR_IMAGE}

    # Add ASPEED FMC header to A1 zephyr image.
    dd if=${UNPACKDIR}/${AST_HEADER_BIN} of=${B}/${OUT_A1_ZEPHYR_IMAGE}  bs=1 seek=0 conv=notrunc
    dd if=${DEPLOY_DIR_IMAGE}/${A1_ZEPHYR_IMAGE} of=${B}/${OUT_A1_ZEPHYR_IMAGE}  bs=1 seek=2560 conv=notrunc #0xA00

    # Bundle image.
    cptra-imgtool \
        create-auth-flash \
        --cfg ${UNPACKDIR}/${CALIPTRA_MANIFEST_CONFIG} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        --man ${B}/${OUT_A1_ZEPHYR_IMAGE} \
        --flash ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE}
}

do_deploy_image() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/${CALIPTRA_MANIFEST_FLASH_IMAGE} ${DEPLOYDIR}
    # Add for generate recovery_zephyr-aspeed-bootmcu-a1.bin
    install -m 644 ${B}/${OUT_A1_ZEPHYR_IMAGE} ${DEPLOYDIR}
}

# Add build AST2700 A1 zephyr-aspeed-bootmcu
do_compile[depends] += " \
    zephyr-aspeed-bootmcu-a1:do_deploy \
    "
