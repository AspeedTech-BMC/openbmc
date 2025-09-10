FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
	file://40-hardware-watchdog.conf \
	"

SRC_URI:append:ast-ufs = " \
	file://90-ufs-only.rules \
	"

SRC_URI:append:ast-mmc = " \
	file://90-mmc-only.rules \
	"

FILES:${PN}:append:ast-ufs = " \
	${nonarch_libdir}/udev/rules.d/90-ufs-only.rules \
	"

FILES:${PN}:append:ast-mmc = " \
  	${nonarch_libdir}/udev/rules.d/90-mmc-only.rules \
	"
