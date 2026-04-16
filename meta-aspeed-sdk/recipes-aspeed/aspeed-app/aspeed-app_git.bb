LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit pkgconfig meson

SRC_URI = "gitsm://github.com/AspeedTech-BMC/aspeed_app.git;protocol=https;branch=${BRANCH}"

PV = "1.0+git"

# Tag for v00.01.22
SRCREV = "4cb20ccffccb818e2458d069148329189368aaa0"
BRANCH = "master"


DEPENDS += "openssl python3-jsonschema-native python3-jinja2-native"
RDEPENDS:${PN} += "openssl"

EXTRA_OEMESON:append:aspeed-g7 = " \
    -Dotp-platform='ast2700' \
"

FILES:${PN}:append = " /usr/share/* "
