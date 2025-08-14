FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://ttyS2.conf"
SRC_URI:append = " file://ttyS7.conf"

SYSTEMD_SERVICE:${PN} = "hostlogger@ttyS2.service"
SYSTEMD_SERVICE:${PN} += "hostlogger@ttyS7.service"

