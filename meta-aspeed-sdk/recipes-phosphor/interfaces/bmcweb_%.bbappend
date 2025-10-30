FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

EXTRA_OEMESON:append = " \
    -Dhttp-body-limit=264 \
    "

# Use the old updater.
EXTRA_OEMESON:append = " \
    -Dredfish-updateservice-use-dbus=disabled \
"

SRC_URI:append = " \
    file://0001-bmcweb-fixes-virtual-media-buffer-overflow.patch \
    file://0002-Support-websocket-control-frame-callback.patch \
    file://0003-Modify-Content-Security-Policy-CSP-to-adapt-WebAssem.patch \
    file://0004-bmcweb-firmware-update-apply-immediate.patch \
    file://override.conf \
    "

# Add bmcweb service override to fix VM fail at Linux-6.12.
FILES:${PN} += "${systemd_system_unitdir}/bmcweb.service.d/override.conf"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}/bmcweb.service.d
    install -m 0644 ${UNPACKDIR}/override.conf \
            ${D}${systemd_system_unitdir}/bmcweb.service.d/override.conf
}
