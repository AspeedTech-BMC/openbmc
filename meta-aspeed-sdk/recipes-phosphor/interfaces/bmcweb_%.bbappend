FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

EXTRA_OEMESON:append= " \
    -Dhttp-body-limit=264 \
    "

SRC_URI:append = " \
    file://0001-bmcweb-fixes-virtual-media-buffer-overflow.patch \
    file://0002-Support-websocket-control-frame-callback.patch \
    file://0003-Modify-Content-Security-Policy-CSP-to-adapt-WebAssem.patch \
    "

