FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

RDEPENDS:${PN}:ast-irot += "bash"

SRC_URI:append:ast-irot = " \
                  file://mctp-local.service \
                  file://mctpd.conf \
                 "

FILES:${PN}:append:ast-irot = " ${systemd_system_unitdir}/* "
SYSTEMD_SERVICE:${PN}:ast-irot += "mctp-local.service"

do_install:append:ast-irot() {
    install -m 0644 ${UNPACKDIR}/mctp-local.service ${D}${systemd_system_unitdir}/
    install -d ${D}/etc/
    install -m 0644 ${UNPACKDIR}/mctpd.conf ${D}/etc/mctpd.conf
}
