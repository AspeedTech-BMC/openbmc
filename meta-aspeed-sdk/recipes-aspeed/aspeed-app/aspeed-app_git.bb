LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit pkgconfig meson

SRC_URI = " gitsm://gerrit.aspeed.com:29418/aspeed_app.git;protocol=ssh;branch=${BRANCH} "

PV = "1.0+git"
SRCREV = "${AUTOREV}"
BRANCH = "develop"


DEPENDS += "openssl python3-jsonschema-native python3-jinja2-native"
RDEPENDS:${PN} += "openssl"

EXTRA_OEMESON:append:aspeed-g7 = " \
    -Dotp-platform='ast2700' \
"

# mctp-i3c and i3c-test are not supported on AST2500, remove them after install
do_install:append:aspeed-g5() {
    rm -f ${D}${bindir}/mctp-i3c
    rm -f ${D}${bindir}/i3c-test
}

FILES:${PN}:append = " /usr/share/* "
