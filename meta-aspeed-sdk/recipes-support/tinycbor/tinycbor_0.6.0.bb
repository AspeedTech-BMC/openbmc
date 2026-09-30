SUMMARY = "TinyCBOR - CBOR encoder and decoder library"
DESCRIPTION = "A small, efficient library for encoding and decoding data in the \
Concise Binary Object Representation (CBOR) format, optimized for embedded targets."
HOMEPAGE = "https://github.com/intel/tinycbor"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=6c1ac30774dd6476b42b5b020cd2aa5f"

SRC_URI = "git://github.com/intel/tinycbor.git;protocol=https;branch=main;tag=v${PV}"
SRCREV = "1183f06488e68587f76309cd9b5a7b9a26ea9c75"

EXTRA_OEMAKE = "prefix=${prefix} libdir=${libdir} includedir=${includedir} bindir=${bindir}"

do_compile() {
    oe_runmake ${EXTRA_OEMAKE}
}

do_install() {
    oe_runmake ${EXTRA_OEMAKE} 'DESTDIR=${D}' install
}

FILES:${PN} = "${libdir}/libtinycbor.so.* ${bindir}/cbordump"
FILES:${PN}-dev = "${includedir}/tinycbor ${libdir}/libtinycbor.so ${libdir}/pkgconfig"

BBCLASSEXTEND = "native nativesdk"
