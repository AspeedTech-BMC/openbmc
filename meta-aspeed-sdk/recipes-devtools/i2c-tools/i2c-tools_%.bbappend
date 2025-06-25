FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://resolve-segmentation-fault-in-i2cdump.patch"
