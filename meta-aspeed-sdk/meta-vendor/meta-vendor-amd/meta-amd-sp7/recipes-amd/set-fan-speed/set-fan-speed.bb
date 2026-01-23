SUMMARY = "ASPEED Fan speed setting service"
DESCRIPTION = "Script for setting Fan speeds at boot time"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd
inherit obmc-phosphor-systemd

DEPENDS = "systemd"
RDEPENDS:${PN} = "bash"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI = " file://set-fan-speed.sh \
            file://set-fan-speed.service \
          "

SYSTEMD_SERVICE:${PN} = "set-fan-speed.service"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/set-fan-speed.sh ${D}${bindir}
}
