SOC_FAMILY = "aspeed-g7"
include conf/machine/include/soc-family.inc
require conf/machine/include/aspeed-sdk.inc

DEFAULTTUNE = "cortexa35"

require conf/machine/include/arm/armv8a/tune-cortexa35.inc
require conf/machine/include/ast-arm-trusted-firmware-a.inc
require conf/machine/include/ast-optee-os.inc

SERIAL_CONSOLES ?= "115200;ttyS12"

# kernel fitimage
# Use lzma compressed algorithm instead of gzip to save the code size.
FIT_KERNEL_COMP_ALG ?= "lzma"
FIT_KERNEL_COMP_ALG_EXTENSION ?= ".lzma"

COMPATIBLE_MACHINE:aspeed-g7 = "aspeed-g7"
MACHINE_FEATURES:append = " ast27x5"
MACHINEOVERRIDES .= ":ast27x5"

# TFA platform configuration.
TFA_PLATFORM = "ast2700"
# optee for BL32.
TFA_SPD = "opteed"
# TFA default build is BL31.
TFA_BUILD_TARGET = "bl31"

# It is a shortcut for "make PLATFORM=aspeed PLATFORM_FLAVOR=ast2700"
OPTEEMACHINE = "aspeed-ast2700"

UBOOT_FIT_ARM_TRUSTED_FIRMWARE_IMAGE ?= "${DEPLOY_DIR_IMAGE}/trusted-firmware-a/${TFA_BUILD_TARGET}.bin"
UBOOT_FIT_TEE_IMAGE ?= "${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin"
UBOOT_FIT_SSP_IMAGE ?= "${DEPLOY_DIR_IMAGE}/zephyr-aspeed-ssp.bin"
UBOOT_FIT_TSP_IMAGE ?= "${DEPLOY_DIR_IMAGE}/zephyr-aspeed-tsp.bin"

# zephyr pre-built toolchain
ZEPHYR_TOOLCHAIN_VARIANT = "zephyr"

# zephyr build
PREFERRED_PROVIDER_virtual/ssp ??= "zephyr-aspeed-ssp"
PREFERRED_PROVIDER_virtual/tsp ??= "zephyr-aspeed-tsp"
PREFERRED_VERSION_zephyr-sdk-native = "0.16.9"
PREFERRED_VERSION_zephyr-kernel ??= "3.7.0"

# caliptra manifest
CPTRA_TOML_FILENAME ?= "ast2705-manifest.toml"
CALIPTRA_MANIFEST_CONFIG_DIR ??= "${DEPLOY_DIR_IMAGE}"
CALIPTRA_MANIFEST_CONFIG ??= "${CPTRA_TOML_FILENAME}"
CALIPTRA_MANIFEST_KEY_DIR ??= "${STAGING_DATADIR_NATIVE}/aspeed-secure-config/ast2755/keys"
CALIPTRA_MANIFEST_FLASH_IMAGE ?= "ast2705-manifest-flash.bin"
CALIPTRA_MANIFEST_SOC_IMAGE ?= "ast2705-soc-manifest.bin"

PREFERRED_VERSION_cptra-imgtool = "2x"
PREFERRED_VERSION_cptra-imgtool-native = "2x"
PREFERRED_VERSION_caliptra-mcu-sw-native = "2x"
PREFERRED_VERSION_aspeed-image-manifest = "2x"
PREFERRED_VERSION_aspeed-manifest-config = "2x"
