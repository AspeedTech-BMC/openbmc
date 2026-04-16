SUMMARY = "SPDM Tool"
DESCRIPTION = "Implementation of the SPDM specifications"
PR = "r1"
PV = "1.0+git"

inherit meson pkgconfig
inherit systemd

require spdm.inc

DEPENDS += "systemd"
DEPENDS += "sdeventplus"
DEPENDS += "phosphor-dbus-interfaces"
DEPENDS += "nlohmann-json"
DEPENDS += "cli11"
DEPENDS += "mbedtls"


# libmctp-externals.h is from https://github.com/NVIDIA/libmctp
SRC_URI += "file://libmctp-externals.h"
SRC_URI += "file://0001-make-spdmd-subdir-optional-in-meson.patch"

EXTRA_OEMESON = " \
        -Dspdmd=disabled \
        -Dsystemd=disabled \
        -Dtests=disabled \
        -Dfetch_serialnumber_from_responder=26 \
        -Dcsm_service_enabled=disabled \
        -Denable-in-kernel-mctp=enabled \
        "

do_configure:prepend() {
    cp ${UNPACKDIR}/libmctp-externals.h ${S}/libspdmcpp/headers_public/
}
