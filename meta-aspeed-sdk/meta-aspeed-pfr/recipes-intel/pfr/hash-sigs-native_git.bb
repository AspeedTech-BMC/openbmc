SUMMARY = "HSS/LMS Signature Scheme Implementation"
DESCRIPTION = "This code attempts to be a usable implementation of \
the LMS Hash Based Signature Scheme from RFC 8554."
HOMEPAGE = "https://github.com/cisco/hash-sigs"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://license.txt;md5=175bba43b4ad4394534eeffe563ae226"

DEPENDS = "openssl-native"
inherit native

SRC_URI = "git://github.com/cisco/hash-sigs.git;protocol=https;branch=master \
           file://0001-support-yocto-build.patch \
           file://0002-to-support-openssl-3.0-library-code.patch \
          "

PV = "1.0+git"
SRCREV = "b0631b8891295bf2929e68761205337b7c031726"


EXPORT_HEADER = "hss.h \
                 common_defs.h \
                 hss_verify.h \
                 hss_common.h \
                 hss_verify_inc.h \
                 hash.h \
                 sha256.h \
                 hss_sign_inc.h \
                 hss_internal.h \
                 config.h \
                 hss_zeroize.h \
                "

do_install () {
    install -d ${D}${libdir}
    install -m 644 hss_verify.a ${D}${libdir}/libhss_verify.a
    install -m 644 hss_lib.a ${D}${libdir}/libhss_lib.a
    install -d ${D}${includedir}/hss
    install -m 644 ${EXPORT_HEADER} ${D}${includedir}/hss
}

