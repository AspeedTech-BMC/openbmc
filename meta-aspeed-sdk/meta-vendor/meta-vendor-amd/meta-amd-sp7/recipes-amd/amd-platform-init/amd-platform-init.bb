SUMMARY = "AMD Platform init"
DESCRIPTION = "Script for AMD platform initial"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit systemd
inherit obmc-phosphor-systemd

DEPENDS = "systemd"
RDEPENDS:${PN} = "bash"

SRC_URI = " file://platform-init.sh \
            file://amd-platform-init.service \
          "

S = "${UNPACKDIR}"

SYSTEMD_SERVICE:${PN} = "amd-platform-init.service"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/platform-init.sh ${D}${bindir}/
}