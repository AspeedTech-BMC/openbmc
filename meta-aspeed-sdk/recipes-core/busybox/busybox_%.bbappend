FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://taskset.cfg "
SRC_URI:append = " file://mkfs.cfg "
SRC_URI:append = " file://dd.cfg "
SRC_URI:append = " file://mpstat.cfg "
SRC_URI:append = " file://crc32.cfg "
# AST2600 and AST2700 use usbutils. AST2500 due to rofs size issues, uses busybox lsusb.
SRC_URI:append:aspeed-g5 = " file://lsusb.cfg "
SRC_URI:append = " file://brctl.cfg "
