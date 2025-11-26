FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
inherit obmc-phosphor-systemd

RDEPENDS:${PN} = " bash "

SRC_URI:append = " \
    file://mctp-init.sh \
    file://mctp-init.conf \
    "

SYSTEMD_OVERRIDE:${PN} += "mctp-init.conf:mctpd.service.d/mctp-init.conf"

do_install:append () {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/mctp-init.sh ${D}${bindir}
}
