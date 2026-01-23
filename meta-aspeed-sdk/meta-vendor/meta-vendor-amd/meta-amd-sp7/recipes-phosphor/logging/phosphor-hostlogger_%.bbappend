FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://ttyS13.conf"

SYSTEMD_SERVICE:${PN} = "hostlogger@ttyS13.service"

