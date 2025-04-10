FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI:append = " file://settings.override.yml"

# The software-update-dbus-interface has several flaws and is not usable.
# The phosphor-software-manager and bmcweb use the old updater.
# Add applyTime override to reboot the BMC immediately after a firmware upgrade.
SRC_URI:append = " file://applyTime.override.yml"
