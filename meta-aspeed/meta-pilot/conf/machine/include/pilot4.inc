#@TYPE: Machine
#@NAME: Aspeed PILOT4
#@DESCRIPTION: Common machine configuration for the Aspeed PILOT4 Chip

require conf/machine/include/aspeed.inc

#UBOOT_MACHINE ?= "ast_g5_ncsi_config"
UBOOT_ENTRYPOINT ?= "0x81008000"
UBOOT_LOADADDRESS ?= "0x81008000"
UBOOT_RD_LOADADDRESS ?= "0x84000000"
UBOOT_RD_ENTRYPOINT ?= "0x84000000"

MACHINE_EXTRA_RDEPENDS += "udev-aspeed-vuart"

DEFAULTTUNE ?= "arm7a-novfp"

SERIAL_CONSOLES = "115200;ttyS0"

SOC_FAMILY = "pilot4_asic"
#include conf/machine/include/soc-family.inc
MACHINEOVERRIDES .= ":pilot4"

require conf/machine/include/tune-arm7a-novfp.inc

