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

SRC_URI += "file://0001-make-spdmd-subdir-optional-in-meson.patch"
SRC_URI += "file://0002-update-sdbusplus-types.patch"

EXTRA_OEMESON = " \
        -Dspdmd=disabled \
        -Dsystemd=disabled \
        -Dtests=disabled \
        -Dfetch_serialnumber_from_responder=26 \
        -Dcsm_service_enabled=disabled \
        -Denable-in-kernel-mctp=enabled \
        "
