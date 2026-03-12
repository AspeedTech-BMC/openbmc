SUMMARY = "ncsi-netlink"
DESCRIPTION = "ncsi-netlink"
PR = "r1"
LICENSE = "GPL-2.0-only"

SRC_URI = "git://github.com/sammj/ncsi-netlink;protocol=https;branch=master"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"
SRCREV = "333ffc4caef79017a9d5d56d69df457b4fb4fcc0"

DEPENDS = "libnl"
RDEPENDS:${PN} = "libnl libnl-genl"

PV = "1.0+git"

TARGET_CC_ARCH += "${LDFLAGS}"

do_compile() {
    export LIBNL_INCDIR="${STAGING_INCDIR}/libnl3"
    export LIBNL_LIBDIR="${STAGING_LIBDIR}"
    oe_runmake
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ncsi-netlink ${D}${bindir}/ncsi-netlink-sammj
}

do_configure[noexec] = "1"
