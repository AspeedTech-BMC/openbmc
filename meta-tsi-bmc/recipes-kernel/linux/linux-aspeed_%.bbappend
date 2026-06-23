# TSI BMC kernel integration hook (Stage 1: ASPEED AST2700 BSP stand-in).
#
# Stage 1 boots the TSI machines on the ASPEED linux-aspeed kernel, so this
# append only establishes the file search path for TSI-specific drop-ins
# (defconfig fragments, device trees) keyed off the "tsi-bmc" machine override.
#
# When the TSI BSP lands (Stage 4+), the kernel port moves to its own recipe
# (linux-tsi) and this hook is retargeted there.

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Example TSI-only customization point (no-op until populated):
# SRC_URI:append:tsi-bmc = " file://tsi.cfg"
