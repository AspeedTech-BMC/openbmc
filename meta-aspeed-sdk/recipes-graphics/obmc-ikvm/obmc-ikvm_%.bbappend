FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://0001-Add-control-for-aspeed-format.patch"
SRC_URI:append = " file://0002-Avoid-frame-drop.patch"
SRC_URI:append = " file://0003-Add-support-of-partial-jpeg.patch"
