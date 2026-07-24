FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

RDEPENDS:${PN} = " bash "

SRC_URI:append = " \
            file://mctp-i3c-starter.sh \
            file://mctp-i3c-state-monitor.sh \
            file://mctp-i3c-state-monitor.service \
            "

SYSTEMD_OVERRIDE:${PN} = "hotjoin.conf:pfr-mctp-i3c.service.d/hotjoin.conf"
SYSTEMD_SERVICE:${PN} += "mctp-i3c-state-monitor.service"

do_install:append() {
    install -m 0755 ${UNPACKDIR}/mctp-i3c-state-monitor.sh ${D}${bindir}/mctp-i3c-state-monitor.sh
}
