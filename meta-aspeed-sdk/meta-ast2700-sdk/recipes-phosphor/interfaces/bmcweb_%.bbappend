FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://0004-bmcweb-support-ast2750-dual-nodes.patch"

SRC_URI:append:ast-irot = " \
    file://0005-task-report-failed-tasks-via-TaskMonitor.patch \
    file://0006-update_service-normalize-multipart-update-target.patch \
"

# meta-aspeed-sdk disables the D-Bus updater by default; re-enable it for
# iRoT so multipart firmware update can target individual FirmwareInventory
# components instead of only the BMC manager.
EXTRA_OEMESON:remove:ast-irot = "-Dredfish-updateservice-use-dbus=disabled"
EXTRA_OEMESON:append:ast-irot = " -Dredfish-updateservice-use-dbus=enabled"
