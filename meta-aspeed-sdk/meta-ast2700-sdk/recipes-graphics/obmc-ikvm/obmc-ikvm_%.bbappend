FILESEXTRAPATHS:append := "${THISDIR}/${PN}:"

SRC_URI:append = " file://start-ipkvm1.service"
SRC_URI:append = " file://create_usbhid.sh"
SRC_URI:append = " file://0004-obmc-ikvm-support-ast2750-dual-nodes.patch"

SYSTEMD_SERVICE:${PN}:append = " start-ipkvm1.service"

FILES:${PN}:append = " \
    ${systemd_system_unitdir}/start-ipkvm1.service \
    ${bindir}/create_usbhid.sh \
"

do_install:append () {
    install -d ${D}${bindir} ${D}${systemd_system_unitdir}

    install -D -m 0644 ${UNPACKDIR}/start-ipkvm1.service ${D}${systemd_system_unitdir}
    install -D -m 0755 ${UNPACKDIR}/create_usbhid.sh ${D}${bindir}
}
