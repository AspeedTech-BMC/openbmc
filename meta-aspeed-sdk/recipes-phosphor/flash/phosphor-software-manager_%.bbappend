FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://0001-bmc-support-ufs-firmware-update.patch"

EXTRA_OEMESON:append:ast-ufs = " -Dmmc-storage-mode='ufs'"

# Use the old updater.
PACKAGECONFIG:remove = "software-update-dbus-interface"
