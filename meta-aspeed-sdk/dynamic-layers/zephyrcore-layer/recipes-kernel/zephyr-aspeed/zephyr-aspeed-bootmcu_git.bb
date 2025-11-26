require recipes-kernel/zephyr-kernel/zephyr-image.inc
require zephyr-aspeed-src.inc

SUMMARY = "BootMCU runtime firmware"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/bootmcu"
PV = "1.0+git"

# aspeed-zephyr-project bootmcu
SRC_URI_ASPEED_ZEPHYR_PROJECT = "gitsm://github.com/AspeedTech-BMC/aspeed-zephyr-project;protocol=https"
ASPEED_ZEPHYR_PROJECT_BRANCH = "aspeed-master"

# Tag for v03.03
SRCREV_bootmcu = "7cfdc08b0bef98a30074fbd473c65a1f1daad076"

SRC_URI += "\
    ${SRC_URI_ASPEED_ZEPHYR_PROJECT};name=bootmcu;branch=${ASPEED_ZEPHYR_PROJECT_BRANCH};destsuffix=git/aspeed-zephyr-project \
"

ZEPHYR_MODULES += "\
${S}/aspeed-zephyr-project\;\
"

ZEPHYR_BOARD_BOOTMCU ??= "ast2700_evb/ast2700/bootmcu"
ZEPHYR_BOARD = "${ZEPHYR_BOARD_BOOTMCU}"
ZEPHYR_MAKE_OUTPUT += "${BOOTMCU_FW_BINARY}"

ZEPHYR_SRC_DIR ??= "${S}/aspeed-zephyr-project/apps/mcu-runtime"

DEPENDS += "fmc-imgtool-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

inherit otptool

# Use fmc-imgtool to create fmc image since A1
# export CRYPTOGRAPHY_OPENSSL_NO_LEGACY variable to fix the following errors.
# OpenSSL 3.0 legacy provider failed to load
# https://github.com/pyca/cryptography/issues/10598
do_create_fmc_image() {
    export CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1

    local ecc_key=""
    local ecc_key_index=""
    local lms_key=""
    local lms_key_index=""
    local sign_args=""

    if [ -f "${FMC_ECC_KEY}" ]; then
        ecc_key="--ecc-key ${FMC_ECC_KEY}"
    fi

    if [ -n "${FMC_ECC_KEY_INDEX}" ]; then
        ecc_key_index="--ecc-key-index ${FMC_ECC_KEY_INDEX}"
    fi

    if [ -f "${FMC_LMS_KEY}" ]; then
        lms_key="--lms-key ${FMC_LMS_KEY}"
    fi

    if [ -n "${FMC_LMS_KEY_INDEX}" ]; then
        lms_key_index="--lms-key-index ${FMC_LMS_KEY_INDEX}"
    fi

    if [ "${FMC_SIGN_ENABLE}" = "1" ]; then
        sign_args="${ecc_key} ${ecc_key_index} ${lms_key} ${lms_key_index}"
    fi

    echo "sign_args=${sign_args}"

    fmc-imgtool \
        --verbose \
        --version 2 \
        --input ${B}/zephyr/zephyr.bin \
        --output ${B}/zephyr/${BOOTMCU_FW_BINARY} \
        --prebuilt-dir ${DEPLOY_DIR_IMAGE}/ \
        ${sign_args}
}

addtask create_fmc_image before do_install after do_compile

do_create_fmc_image[depends] += " \
    fmc-images:do_deploy \
    "

