LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit pkgconfig meson

SRC_URI = "git://github.com/AspeedTech-BMC/aspeed_app.git;protocol=https;branch=${BRANCH}"

PV = "1.0+git"

# Tag for v00.01.20
SRCREV = "3dbdc7e3f753235442473e1ad62ac762a3bd884e"
BRANCH = "master"

S = "${WORKDIR}/git"

DEPENDS += "openssl"
RDEPENDS:${PN} += "openssl"

EXTRA_OEMESON:append:aspeed-g7 = " \
    -Dotp-platform='ast2700' \
"

FILES:${PN}:append = " /usr/share/* "
