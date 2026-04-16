SUMMARY = "Test application for libmctp-intel stack"
DESCRIPTION = "Test application for libmctp-intel stack"
PR = "r0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

S = "${UNPACKDIR}"

SRC_URI = " file://mctp-astpcie-test.c \
            file://mctp-astpcie-test.h \
            file://mctp-smbus-test.c \
            file://mctp-smbus-test.h \
            file://mctp-test-utils.c \
            file://mctp-test-utils.h \
            file://CMakeLists.txt \
          "

DEPENDS = "libmctp-intel"
inherit cmake
