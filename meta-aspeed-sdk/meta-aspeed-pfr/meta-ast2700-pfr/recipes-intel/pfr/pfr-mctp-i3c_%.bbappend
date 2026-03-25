FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
            file://mctp-i3c-starter.sh \
            "

SYSTEMD_OVERRIDE:${PN} = "hotjoin.conf:pfr-mctp-i3c.service.d/hotjoin.conf"
