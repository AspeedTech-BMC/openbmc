SUMMARY = "Aspeed Crypto Engine for openssl"
HOMEPAGE = "https://github.com/AspeedTech-BMC/ast_crypto_engine"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c75985e733726beaba57bc5253e96d04"

SRC_URI = "git://github.com/AspeedTech-BMC/ast_crypto_engine;branch=master;protocol=https"

inherit cmake pkgconfig

PV = "1.0+git"

# Tag for v01.01
SRCREV = "90632cf34bd32905151bee8ee0b7c1ad9d09552b"

S = "${WORKDIR}/git"

DEPENDS = "openssl"
RDEPENDS:${PN} = "openssl"

do_install () {
    install -d ${D}${libdir}/engines-3
    install -m 0755 ${B}/ast_crypto_engine.so ${D}${libdir}/engines-3
}

FILES:${PN} += "${libdir}/engines-3/*.so"