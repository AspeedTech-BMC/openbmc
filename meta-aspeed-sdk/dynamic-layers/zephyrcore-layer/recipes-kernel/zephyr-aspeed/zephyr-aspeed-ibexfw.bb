require recipes-kernel/zephyr-kernel/zephyr-image.inc
require zephyr-aspeed-src.inc

SUMMARY = "BootMCU runtime firmware"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/ibexfw"
PV = "1.0+git"

SRCREV_zephyr = "${AUTOREV}"
ZEPHYR_BRANCH = "aspeed-dev-v3.7.0"

ZEPHYR_BOARD_IBEXFW ??= "ast2700_evb/ast2700/bootmcu"
ZEPHYR_BOARD = "${ZEPHYR_BOARD_IBEXFW}"

ZEPHYR_SRC_DIR ??= "${ZEPHYR_BASE}/samples/boards/ast2700_evb/demo"
