SUMMARY = "AspeedTech BMC Co-Processor Package Group"

PR = "r2"

PACKAGE_ARCH="${TUNE_PKGARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"
RPROVIDES:${PN} = "${PACKAGES}"

PACKAGES = " \
    ${PN}-ssp \
    "

SUMMARY:${PN}-ssp = "AspeedTech Secondary Service Processor"
RDEPENDS:${PN}-ssp = " \
    virtual-ssp \
    "
RRECOMMENDS:${PN}-ssp= " \
    kernel-module-aspeed-ssp \
    "