FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

DEPENDS = "jpeg"
RDEPENDS:${PN} = "jpeg"

SRC_URI:append = " http://s-tech.elsat.net.pl/fbv/fbv-1.0b.tar.gz "
SRC_URI:append = " file://meson.build "

SRC_URI[sha256sum] = "9b55b9dafd5eb01562060d860e267e309a1876e8ba5ce4d3303484b94129ab3c"

LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM:append = " file://COPYING;md5=130f9d9dddfebd2c6ff59165f066e41c "

inherit meson

do_configure:prepend() {
  cp ${UNPACKDIR}/meson.build ${S}
  cd ${S}
  ./configure --without-libungif --without-libpng
}

