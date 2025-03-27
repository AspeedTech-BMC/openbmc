SUMMARY = "Aspeed Crypto Engine for openssl"
HOMEPAGE = "https://github.com/AspeedTech-BMC/ast_crypto_engine"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c75985e733726beaba57bc5253e96d04"

SRC_URI = "git://gerrit.aspeed.com:29418/ast_crypto_engine;branch=master;protocol=ssh"

inherit cmake pkgconfig

PV = "1.0+git"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

DEPENDS = "openssl"
RDEPENDS:${PN} = "openssl"

do_install () {
    install -d ${D}${libdir}/engines-3
    install -m 0755 ${B}/ast_crypto_engine.so ${D}${libdir}/engines-3
}

FILES:${PN} += "${libdir}/engines-3/*.so"