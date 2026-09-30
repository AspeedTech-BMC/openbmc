SUMMARY = "NVIDIA SPDM Stack"
DESCRIPTION = "NVIDIA implementation of the SPDM specifications"
PR = "r1"
PV = "1.0+git"

inherit meson pkgconfig systemd

require spdm.inc

DEPENDS += " \
    systemd \
    sdeventplus \
    phosphor-dbus-interfaces \
    nlohmann-json \
    cli11 \
    mbedtls \
"

EXTRA_OEMESON = " \
    -Dsystemd=disabled \
    -Dtests=disabled \
    -Dfetch_serialnumber_from_responder=26 \
    -Dcsm_service_enabled=disabled \
    -Denable-in-kernel-mctp=enabled \
"
