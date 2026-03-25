SUMMARY = "MCTP Daemon for PFR 4.0/5.0"
DESCRIPTION = "MCTP Daemon for communicating with AST1060 via i3c"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit pkgconfig meson

S = "${UNPACKDIR}"

SRC_URI = " file://main.c \
            file://meson.build \
            file://pfr-mctp-i3c.service \
            file://mctp-i3c-starter.sh \
          "

inherit obmc-phosphor-systemd
SYSTEMD_SERVICE:${PN} = "pfr-mctp-i3c.service"

