require recipes-kernel/zephyr-kernel/zephyr-image.inc
require zephyr-aspeed-src.inc

SUMMARY = "The Secondary Service Processor (SSP) firmware"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/ssp"
PV = "1.0+git"

ZEPHYR_BOARD_SSP ??= "ast2700_evb/ast2700/ssp_tsp"
ZEPHYR_BOARD = "${ZEPHYR_BOARD_SSP}"

ZEPHYR_SRC_DIR ??= "${ZEPHYR_BASE}/samples/subsys/shell/shell_module"
