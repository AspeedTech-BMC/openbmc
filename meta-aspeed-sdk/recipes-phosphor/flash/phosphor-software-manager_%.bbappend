FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast-ufs = " file://0001-support-UFS-storage.patch \
                         "

# Use the old updater.
PACKAGECONFIG:remove = "software-update-dbus-interface"
