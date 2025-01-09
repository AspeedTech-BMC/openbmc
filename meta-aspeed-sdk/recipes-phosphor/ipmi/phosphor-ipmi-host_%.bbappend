FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGECONFIG:append = " dynamic-sensors"
SRC_URI += "file://0001-transporthandler-fix-ipmid-crash.patch"

