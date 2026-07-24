FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
inherit obmc-phosphor-systemd

RDEPENDS:${PN} = " bash "

SRC_URI:append = " \
    file://mctp-init.sh \
    file://mctp-init.conf \
    file://mctpd.conf \
    "

SYSTEMD_OVERRIDE:${PN} += "mctp-init.conf:mctpd.service.d/mctp-init.conf"

do_install:append () {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/mctp-init.sh ${D}${bindir}
    install -d ${D}/etc/
    install -m 0644 ${UNPACKDIR}/mctpd.conf ${D}/etc/mctpd.conf
}
