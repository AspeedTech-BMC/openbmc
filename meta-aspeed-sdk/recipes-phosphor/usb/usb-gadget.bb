SUMMARY = "Turn On USB gadget"
DESCRIPTION = "Script to turn on usb gadget after BMC is ready"

RDEPENDS:${PN} = "aspeed-app"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"
SRC_URI = "file://usbA-net.sh \
           file://usbB-net.sh \
           file://usbA-rndis.sh \
           file://usbB-rndis.sh \
           file://usbA-ms.sh \
           file://usbB-ms.sh \
           file://usbA-uart.sh \
           file://usbB-uart.sh \
           file://netusb.service \
           file://keyboard.sh \
           file://mouse.sh \
          "

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit systemd

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/netusb.service ${D}${systemd_system_unitdir}
    install -d ${D}${bindir}
    install -m 0755 ${S}/usbA-net.sh ${D}/${bindir}/usbA-net.sh
    install -m 0755 ${S}/usbB-net.sh ${D}/${bindir}/usbB-net.sh
    install -m 0755 ${S}/usbA-rndis.sh ${D}/${bindir}/usbA-rndis.sh
    install -m 0755 ${S}/usbB-rndis.sh ${D}/${bindir}/usbB-rndis.sh
    install -m 0755 ${S}/usbA-ms.sh ${D}/${bindir}/usbA-ms.sh
    install -m 0755 ${S}/usbB-ms.sh ${D}/${bindir}/usbB-ms.sh
    install -m 0755 ${S}/usbA-uart.sh ${D}/${bindir}/usbA-uart.sh
    install -m 0755 ${S}/usbB-uart.sh ${D}/${bindir}/usbB-uart.sh
    install -m 0755 ${S}/keyboard.sh ${D}/${bindir}/keyboard.sh
    install -m 0755 ${S}/mouse.sh ${D}/${bindir}/mouse.sh
}

SYSTEMD_SERVICE:${PN} += " netusb.service"
SYSTEMD_AUTO_ENABLE:${PN} = "disable"
