SUMMARY = "MCTP Daemon for PFR 4.0"
DESCRIPTION = "MCTP Daemon for communicating with AST1060 via i3c"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit pkgconfig meson

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"
SRC_URI = " file://main.c;subdir=${S} \
            file://meson.build;subdir=${S} \
            file://pfr-mctp-i3c.service;subdir=${S} \
            file://mctp-i3c-starter.sh;subdir=${S} \
          "

inherit obmc-phosphor-systemd
SYSTEMD_SERVICE:${PN} = "pfr-mctp-i3c.service"

