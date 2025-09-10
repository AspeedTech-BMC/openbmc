FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

inherit obmc-phosphor-systemd

SRC_URI:append = " file://lpcsnoop1.service"
SRC_URI:append = " file://0001-Add-multi-node-support.patch"

SYSTEMD_SERVICE:${PN}:append = " lpcsnoop1.service"

