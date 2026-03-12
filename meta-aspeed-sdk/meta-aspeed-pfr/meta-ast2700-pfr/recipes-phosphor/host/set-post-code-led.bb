SUMMARY = "ASPEED transfer BIOS post code to LED"
DESCRIPTION = "Monitor BIOS post code to set GPIO LED value"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd
inherit obmc-phosphor-systemd

DEPENDS = "systemd"
RDEPENDS:${PN} = "bash"

FILESEXTRAPATHS:append := "${THISDIR}/${PN}:"

SYSTEMD_SERVICE:${PN} = " set-post-code-led.service"

S = "${UNPACKDIR}"

SRC_URI = " file://set-post-code-led.sh \
          "

do_install:append() {
    install -d ${D}${sbindir}
    install -m 0755 ${UNPACKDIR}/set-post-code-led.sh ${D}${sbindir}
}
