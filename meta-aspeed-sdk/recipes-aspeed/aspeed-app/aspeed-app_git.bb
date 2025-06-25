LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

inherit pkgconfig meson

SRC_URI = "git://github.com/AspeedTech-BMC/aspeed_app.git;protocol=https;branch=${BRANCH}"

PV = "1.0+git"

# Tag for v00.01.17
SRCREV = "509eb3839108bdfa52ff55129f19718021ee485c"
BRANCH = "master"

S = "${WORKDIR}/git"

DEPENDS += "openssl"
RDEPENDS:${PN} += "openssl"

FILES:${PN}:append = " /usr/share/* "
