SUMMARY = "Turn On USB gadget"
DESCRIPTION = "Script to turn on usb gadget after BMC is ready"

RDEPENDS:${PN} = "aspeed-app"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"
SRC_URI = "file://usb-net.sh \
           file://usb-rndis.sh \
           file://usb-ms.sh \
           file://usb-uart.sh \
           file://netusb.service \
           file://keyboard.sh \
           file://mouse.sh \
           file://usb-gadget.conf \
          "

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit systemd

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/netusb.service ${D}${systemd_system_unitdir}
    install -d ${D}${bindir}
    install -m 0755 ${S}/usb-net.sh ${D}/${bindir}/usb-net.sh
    install -m 0755 ${S}/usb-rndis.sh ${D}/${bindir}/usb-rndis.sh
    install -m 0755 ${S}/usb-ms.sh ${D}/${bindir}/usb-ms.sh
    install -m 0755 ${S}/usb-uart.sh ${D}/${bindir}/usb-uart.sh
    install -m 0755 ${S}/keyboard.sh ${D}/${bindir}/keyboard.sh
    install -m 0755 ${S}/mouse.sh ${D}/${bindir}/mouse.sh
    install -d ${D}${sysconfdir}
    install -m 0644 ${S}/usb-gadget.conf ${D}${sysconfdir}/usb-gadget.conf
}

SYSTEMD_SERVICE:${PN} += " netusb.service"
SYSTEMD_AUTO_ENABLE:${PN} = "disable"
