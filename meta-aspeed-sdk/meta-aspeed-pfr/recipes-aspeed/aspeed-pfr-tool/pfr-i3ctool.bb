SUMMARY = "PFR I3C Tool for mailbox over mctp"
DESCRIPTION = "I3C tool for communicating with AST1060 via i3c"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit pkgconfig meson

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"
SRC_URI = " file://main.c;subdir=${S} \
            file://meson.build;subdir=${S} \
          "

