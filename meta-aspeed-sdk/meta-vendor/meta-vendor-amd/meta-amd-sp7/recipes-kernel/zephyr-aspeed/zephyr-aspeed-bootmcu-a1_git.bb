require recipes-kernel/zephyr-kernel/zephyr-image.inc
require dynamic-layers/zephyrcore-layer/recipes-kernel/zephyr-aspeed/zephyr-aspeed-src.inc
require dynamic-layers/zephyrcore-layer/recipes-kernel/zephyr-aspeed/zephyr-aspeed-project-src.inc

SUMMARY = "BootMCU runtime firmware for AST2700 A1"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PV = "1.0+git"

ZEPHYR_BOARD = "ast2700_evb/ast2700_a1/bootmcu"
ZEPHYR_SRC_DIR ??= "${S}/aspeed-zephyr-project/apps/mcu-runtime"
ASPEED_ZEPHYR_PROJECT_SUBMODULE_DTC = "1"
EXTRA_OECMAKE += "-DOVERLAY_CONFIG=boards/ast2700_evb_ast2700_a1_bootmcu_force_man_bundle_offset.conf"
