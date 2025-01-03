SUMMARY = "AspeedTech BMC PFR Package Group"

PR = "r1"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"
RPROVIDES:${PN} = "${PACKAGES}"

PACKAGES = " \
    ${PN}-apps \
    "

SUMMARY:${PN}-apps = "AspeedTech PFR App package"
RDEPENDS:${PN}-apps = " \
    aspeed-pfr-tool \
    spdm-emu \
    pfr-mctp-i3c \
    pfr-i3ctool \
    "

