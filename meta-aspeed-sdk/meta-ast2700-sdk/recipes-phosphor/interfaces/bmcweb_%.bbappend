FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://0004-bmcweb-support-ast2750-dual-nodes.patch"

SRC_URI:append:ast-irot = " \
    file://0005-task-report-failed-tasks-via-TaskMonitor.patch \
    file://0006-update_service-normalize-multipart-update-target.patch \
    file://0001-redfish-add-ComponentIntegrity-resources.patch \
    file://0002-redfish-discover-component-integrity-by-interface.patch \
    file://0003-redfish-add-platform-Composite-EAT-API.patch \
    file://0004-http-prefer-literal-routes-over-parameters.patch \
    file://0005-redfish-harden-ComponentIntegrity-and-Composite-EAT.patch \
"

SRCREV:ast-irot = "4b6220f877c92cd0ee7106754ec1afc81e39ad33"

# meta-aspeed-sdk disables the D-Bus updater by default; re-enable it for
# iRoT so multipart firmware update can target individual FirmwareInventory
# components instead of only the BMC manager.
EXTRA_OEMESON:remove:ast-irot = "-Dredfish-updateservice-use-dbus=disabled"
EXTRA_OEMESON:append:ast-irot = " -Dredfish-updateservice-use-dbus=enabled -Dredfish-component-integrity=enabled -Dredfish-composite-eat=enabled "
