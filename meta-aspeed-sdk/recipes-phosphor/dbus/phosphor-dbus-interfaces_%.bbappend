FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = "${@oe.utils.conditional('SOC_FAMILY', 'aspeed-g5', '', ' file://0001-Add-NVIDIA-SPDM-responder-D-Bus-interface.patch', d)}"
