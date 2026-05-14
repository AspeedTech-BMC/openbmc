require recipes-kernel/zephyr-kernel/zephyr-image.inc
require zephyr-aspeed-src.inc
require zephyr-aspeed-project-src.inc

SUMMARY = "AST2700 SSP ASPEED-IROT Firmware"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/ssp"
PV = "1.0+git"

ZEPHYR_BOARD_SSP ?= "ast2700_evb/ast2700/ssp"
ZEPHYR_BOARD = "${ZEPHYR_BOARD_SSP}"

ZEPHYR_SRC_DIR ??= "${S}/aspeed-zephyr-project/apps/aspeed-irot"
ASPEED_ZEPHYR_PROJECT_SUBMODULE_LIBSPDM = "1"
