# Refer to meta-zephyr/meta-zephyr-core/recipes-kernel/zephyr-kernel/zephyr-kernel-src-x.x.x.inc
# creating this file

ZEPHYR_BRANCH = "aspeed-dev-v3.7.0"
SRCREV_zephyr = "${AUTOREV}"
SRCREV_mbedtls = "a78176c6ff0733ba08018cba4447bd3f20de7978"
SRCREV_cmsis = "4b96cbb174678dcd3ca86e11e1f24bc5f8726da0"

SRC_URI_ZEPHYR = "git://gerrit.aspeed.com:29418/zephyr;protocol=ssh"
SRC_URI_ZEPHYR_MBEDTLS = "git://github.com/zephyrproject-rtos/mbedtls;protocol=https"
SRC_URI_ZEPHYR_CMSIS = "git://github.com/zephyrproject-rtos/cmsis;protocol=https"

SRC_URI = "\
    ${SRC_URI_ZEPHYR};name=zephyr;branch=${ZEPHYR_BRANCH};destsuffix=${S}/zephyr \
    ${SRC_URI_ZEPHYR_MBEDTLS};name=mbedtls;nobranch=1;destsuffix=${S}/modules/crypto/mbedtls \
    ${SRC_URI_ZEPHYR_CMSIS};name=cmsis;nobranch=1;destsuffix=${S}/modules/hal/cmsis \
"

ZEPHYR_MODULES = "\
${S}/modules/crypto/mbedtls\;\
${S}/modules/hal/cmsis\;\
"

INSANE_SKIP:${PN} += "arch"

INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
