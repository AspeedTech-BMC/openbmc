# TSI BMC U-Boot integration hook (Stage 1: ASPEED AST2700 BSP stand-in).
#
# Stage 1 boots the TSI machines on the ASPEED AST2700 U-Boot, so this append
# only establishes the file search path for TSI-specific drop-ins (defconfig
# fragments, env, signing keys) keyed off the "tsi-bmc" machine override.
#
# When the TSI BSP lands (Stage 4+), the U-Boot port moves to its own recipe
# (u-boot-tsi) and this hook is retargeted there.

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Example TSI-only customization point (no-op until populated):
# SRC_URI:append:tsi-bmc = " file://tsi-uboot.cfg"
