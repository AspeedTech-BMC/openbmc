SOC_FAMILY = "aspeed-g6"
include conf/machine/include/soc-family.inc
require conf/machine/include/aspeed-sdk.inc

SERIAL_CONSOLES ?= "115200;ttyS4"

DEFAULTTUNE = "armv7ahf-vfpv4d16"
UBOOT_ENTRYPOINT ?= "0x80000000"
UBOOT_LOADADDRESS ?= "0x80000000"
UBOOT_FIT_TEE_LOADADDRESS ?= "0xb0000000"
UBOOT_FIT_TEE_ENTRYPOINT ?= "0xb0000000"

include conf/machine/include/arm/arch-armv7a.inc

COMPATIBLE_MACHINE:aspeed-g6 = "aspeed-g6"
# It is a shortcut for "make PLATFORM=aspeed PLATFORM_FLAVOR=ast2600"
OPTEEMACHINE = "aspeed-ast2600"

UBOOT_FIT_TEE_IMAGE ?= "${DEPLOY_DIR_IMAGE}/optee/tee-raw.bin"

PREFERRED_VERSION_u-boot-aspeed-sdk = "v2019.04+%"
PREFERRED_VERSION_u-boot-fw-utils-aspeed-sdk = "v2019.04+%"
PREFERRED_RPROVIDER_virtual/ssp ?= "ssp"
