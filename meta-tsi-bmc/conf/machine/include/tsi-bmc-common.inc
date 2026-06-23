#@TYPE: Machine include
#@NAME: tsi-bmc-common
#@DESCRIPTION: Common TSI BMC machine configuration
#
# Shared base for every tsi-bmc-* machine. Stage 1 wires in the ASPEED AST2700
# SDK as a BSP stand-in (boot ROM -> Ibex SPL -> Caliptra -> TF-A BL31 ->
# OP-TEE -> U-Boot -> Linux), so the OpenBMC OOB stack can be built and booted
# on QEMU before the real TSI BSP exists. Stages 3/4 replace these ASPEED
# includes and providers with the TSI BSP for the silicon/TSISIM machines.
#
# Per-machine confs that include this file only need to set their own identity
# bits: UBOOT_DEVICETREE and KERNEL_DEVICETREE (and, later, any TSI BSP
# overrides). The 64 MiB TSI flash layout comes from tsi-flash-layout.inc.

# --- ASPEED AST2700 BSP stand-in (Stage 1) ---------------------------------
require conf/machine/include/ast2700-sdk.inc
require conf/machine/include/ast-ssp.inc
require conf/machine/include/ast-tsp.inc
require conf/machine/include/ast-caliptra.inc
require conf/machine/include/ast-ibexfw.inc
require conf/machine/include/obmc-bsp-common.inc

# --- TSI 64 MiB NOR flash layout -------------------------------------------
require conf/machine/include/tsi-flash-layout.inc

# Mark every TSI machine so bbappends / EM configs can key off it.
MACHINEOVERRIDES =. "tsi-bmc:"

# --- Firmware blobs for the AST2700 stand-in boot chain ---------------------
CALIPTRA_FW_BINARY ?= "ast2700-caliptra-fw.bin"
BOOTMCU_MACHINE ?= "ibex-ast2700_defconfig"
BOOTMCU_FW_BINARY ?= "ast2700-ibex-spl.bin"

# --- U-Boot / kernel build defaults (common across TSI machines) -----------
UBOOT_MACHINE ?= "evb-ast2700_defconfig"
UBOOT_SIGN_ENABLE ?= "1"
UBOOT_FITIMAGE_ENABLE ?= "1"
KBUILD_DEFCONFIG ?= "aspeed_g7_defconfig"

# --- OpenBMC machine features (out-of-band management) ----------------------
MACHINE_FEATURES += "\
        obmc-phosphor-fan-mgmt \
        obmc-phosphor-chassis-mgmt \
        obmc-phosphor-flash-mgmt \
        obmc-host-ipmi \
        obmc-host-state-mgmt \
        obmc-chassis-state-mgmt \
        obmc-bmc-state-mgmt \
        "

PREFERRED_PROVIDER_virtual/obmc-system-mgmt ?= "packagegroup-aspeed-obmc"
PREFERRED_PROVIDER_virtual/obmc-host-ipmi-hw ?= "phosphor-ipmi-kcs"
VIRTUAL-RUNTIME_obmc-host-state-manager ?= "x86-power-control"
VIRTUAL-RUNTIME_obmc-chassis-state-manager ?= "x86-power-control"

EXTRA_IMAGEDEPENDS:append = " aspeed-image-recoveryuart"
