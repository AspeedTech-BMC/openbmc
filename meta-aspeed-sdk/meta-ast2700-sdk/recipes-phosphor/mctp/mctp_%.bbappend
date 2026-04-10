FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

RDEPENDS:${PN}:ast2700-irot += "bash"

SRC_URI:append:ast2700-irot = " \
                  file://mctp-local.service \
                  file://mctpd.conf \
                 "

FILES:${PN}:append:ast2700-irot = " ${systemd_system_unitdir}/* "
SYSTEMD_SERVICE:${PN}:ast2700-irot += "mctp-local.service"

do_install:append:ast2700-irot() {
    install -m 0644 ${UNPACKDIR}/mctp-local.service ${D}${systemd_system_unitdir}/
    install -d ${D}/etc/
    install -m 0644 ${UNPACKDIR}/mctpd.conf ${D}/etc/mctpd.conf
}
