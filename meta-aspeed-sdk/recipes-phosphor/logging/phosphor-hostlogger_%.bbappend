FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
	file://ttyS2.conf \
	"

FILES:${PN} += "${systemd_system_unitdir}/hostlogger@.service"
SYSTEMD_SERVICE:${PN} = "hostlogger@ttyS2.service"
