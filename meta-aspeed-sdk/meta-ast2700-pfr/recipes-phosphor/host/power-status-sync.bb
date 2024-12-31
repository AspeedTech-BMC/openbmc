SUMMARY = "ASPEED polling GPIO value and update to DBus"
DESCRIPTION = "Polling GPIO and update value to DBus"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd
inherit obmc-phosphor-systemd

DEPENDS = "systemd"
RDEPENDS:${PN} = "bash"

FILESEXTRAPATHS:append := "${THISDIR}/${PN}:"

SYSTEMD_SERVICE:${PN} = " power-status-sync.service"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI = " file://power-status-sync.sh \
          "

do_install:append() {
    install -d ${D}${sbindir}
    install -m 0755 ${UNPACKDIR}/power-status-sync.sh ${D}${sbindir}
}