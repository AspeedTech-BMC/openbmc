FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SYSTEMD_OVERRIDE:${PN} += "hotjoin.conf:pfr-mctp-i3c.service.d/hotjoin.conf"
