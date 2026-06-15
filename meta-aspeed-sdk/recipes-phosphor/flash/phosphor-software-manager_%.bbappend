FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://0001-bmc-support-ufs-firmware-update.patch \
    file://0002-bmc-add-aspeed-image-verify.patch \
"

# For aspeed image verify
SRC_URI:append:ast-update-verify = " \
    file://aspeed-image-verify.sh \
    file://aspeed-image-verify@.service \
"

EXTRA_OEMESON:append:ast-ufs = " -Dmmc-storage-mode='ufs'"

# Use the old updater.
PACKAGECONFIG:remove = "software-update-dbus-interface"

# For aspeed image verify
PACKAGECONFIG:append:ast-update-verify = " aspeed_image_verify"

PACKAGECONFIG[aspeed_image_verify] = "-Daspeed-verify=enabled, -Daspeed-verify=disabled"

SYSTEMD_SERVICE:${PN}-updater:append:ast-update-verify = " aspeed-image-verify@.service"

do_install:append:ast-update-verify() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0755 ${UNPACKDIR}/aspeed-image-verify.sh ${D}${bindir}/aspeed-image-verify.sh
    install -m 0644 ${UNPACKDIR}/aspeed-image-verify@.service ${D}${systemd_system_unitdir}/
}

FILES:${PN}-updater:append:ast-update-verify = " \
    ${bindir}/aspeed-image-verify.sh \
"

RDEPENDS:${PN}-updater:append:ast-update-verify = " openssl-bin"
