SUMMARY = "BMC Boot Done"
DESCRIPTION = ""

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit obmc-phosphor-systemd

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI = " \
	file://bmc-boot-done.sh \
	file://xyz.openbmc_project.bmc_boot_done.service \
"

SYSTEMD_SERVICE:${PN} = "xyz.openbmc_project.bmc_boot_done.service"
RDEPENDS:${PN} = " bash "

do_install:append() {
	install -d ${D}${systemd_system_unitdir}
	install -m 0644 ${UNPACKDIR}/xyz.openbmc_project.bmc_boot_done.service ${D}${systemd_system_unitdir}/

	install -d ${D}${bindir}
	install -m 0755 ${UNPACKDIR}/bmc-boot-done.sh ${D}${bindir}/
}
