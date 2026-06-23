#@TYPE: Machine include
#@NAME: tsi-flash-layout
#@DESCRIPTION: TSI BMC 128 MiB SPI-NOR flash layout
#
# The TSI QEMU bring-up uses a 128 MiB NOR (FLASH_SIZE = "131072"), matching the
# reference ast2700-default and the soldered w25q01jv chip emulated by the
# ast2700a1-evb QEMU model. Setting FLASH_SIZE here turns on the
# ":flash-131072" override band, which the offsets below populate.
#
# Stage 1 reuses the ASPEED AST2700 boot chain as a stand-in, so the layout
# carries the AST2700-specific front matter (Caliptra RoT + RISC-V Ibex
# BootMCU/SPL merged ahead of U-Boot). These two regions are AST2700-specific
# and are expected to shrink/disappear once the TSI BSP boot chain replaces
# them in Stage 3/4; the U-Boot/kernel/ROFS/RWFS bands are the durable part of
# the TSI layout. Offsets (KiB) mirror the reference 128 MiB AST2700 layout in
# meta-ast2700-sdk's ast2700-default.conf.

FLASH_SIZE = "131072"

# AST2700 stand-in front matter (Stage 1 only; replaced by TSI BSP boot chain).
FLASH_CALIPTRA_SIZE:flash-131072 ?= "128"
FLASH_BMCU_SIZE:flash-131072 ?= "896"

# Durable TSI bands.
FLASH_UBOOT_OFFSET:flash-131072 ?= "0"
FLASH_UBOOT_ENV_OFFSET:flash-131072 ?= "4096"
FLASH_KERNEL_OFFSET:flash-131072 ?= "4224"
FLASH_ROFS_OFFSET:flash-131072 ?= "13440"
FLASH_RWFS_OFFSET:flash-131072 ?= "98304"
RWFS_SIZE ?= "33554432"
