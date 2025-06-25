FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

inherit obmc-phosphor-systemd

SRCREV = "789dab8fed600a885a8073387eea6d363f21ed68"

SRC_URI:append = " file://lpcsnoop1.service"
SRC_URI:append = " file://0001-Add-multi-node-support.patch"

SYSTEMD_SERVICE:${PN}:append = " lpcsnoop1.service"

