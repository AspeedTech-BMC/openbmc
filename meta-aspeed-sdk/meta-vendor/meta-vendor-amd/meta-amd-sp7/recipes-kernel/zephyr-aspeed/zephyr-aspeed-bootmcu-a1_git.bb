require recipes-kernel/zephyr-kernel/zephyr-image.inc
require dynamic-layers/zephyrcore-layer/recipes-kernel/zephyr-aspeed/zephyr-aspeed-src.inc

SUMMARY = "BootMCU runtime firmware for AST2700 A1"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PV = "1.0+git"

# aspeed-zephyr-project bootmcu
SRC_URI_ASPEED_ZEPHYR_PROJECT = "gitsm://github.com/AspeedTech-BMC/aspeed-zephyr-project;protocol=https"
ASPEED_ZEPHYR_PROJECT_BRANCH = "aspeed-master"

# Tag for v03.05
SRCREV_bootmcu = "e27a46c16a643b6aed40cda0d9adfcc7054f5e1a"

SRC_URI += "\
    ${SRC_URI_ASPEED_ZEPHYR_PROJECT};name=bootmcu;branch=${ASPEED_ZEPHYR_PROJECT_BRANCH};destsuffix=git/aspeed-zephyr-project \
"

ZEPHYR_MODULES:append = "\
${S}/aspeed-zephyr-project\;\
"

ZEPHYR_BOARD = "ast2700_evb/ast2700_a1/bootmcu"

ZEPHYR_SRC_DIR ??= "${S}/aspeed-zephyr-project/apps/mcu-runtime"

EXTRA_OECMAKE += "-DOVERLAY_CONFIG=boards/ast2700_evb_ast2700_a1_bootmcu_force_man_bundle_offset.conf"
