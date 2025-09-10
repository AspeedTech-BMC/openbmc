FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGECONFIG:append = " dynamic-sensors"

# Use this version to fix dbus-sdr error
SRCREV = "724c74b11d6e53fc6dce685a261d81eaa5813c4a"
