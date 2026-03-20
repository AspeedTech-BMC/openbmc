SUMMARY = "Test application for libmctp-intel stack"
DESCRIPTION = "Test application for libmctp-intel stack"
PR = "r0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

S = "${UNPACKDIR}"

SRC_URI = " file://mctp-astpcie-test.c;subdir=${S} \
            file://mctp-astpcie-test.h;subdir=${S} \
            file://mctp-smbus-test.c;subdir=${S} \
            file://mctp-smbus-test.h;subdir=${S} \
            file://mctp-test-utils.c;subdir=${S} \
            file://mctp-test-utils.h;subdir=${S} \
            file://CMakeLists.txt;subdir=${S} \
          "

DEPENDS = "libmctp-intel"
inherit cmake
