FILESEXTRAPATHS:append := "${THISDIR}/${PN}:"

SRC_URI:append = " file://obmc-ikvm1.service"
SRC_URI:append = " file://create_usbhid.sh"
SRC_URI:append = " file://0004-obmc-ikvm-support-ast2750-dual-nodes.patch"
SRC_URI:append = " file://0005-obmc-ikvm-support-ast2700-A1.patch"

SYSTEMD_SERVICE:${PN}:append = " obmc-ikvm1.service"

FILES:${PN}:append = " \
    ${systemd_system_unitdir}/obmc-ikvm1.service \
    ${bindir}/create_usbhid.sh \
"

do_install:append () {
    install -d ${D}${bindir} ${D}${systemd_system_unitdir}

    install -D -m 0644 ${UNPACKDIR}/obmc-ikvm1.service ${D}${systemd_system_unitdir}
    install -D -m 0755 ${UNPACKDIR}/create_usbhid.sh ${D}${bindir}
}
