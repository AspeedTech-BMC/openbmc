SUMMARY = "Intel PFR Package Group"

PR = "r1"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"
RPROVIDES:${PN} = "${PACKAGES}"

PACKAGES = " \
    ${PN}-apps \
    "

SUMMARY:${PN}-apps = "Intel PFR App package"
RDEPENDS:${PN}-apps = " \
    obmc-pfr-image \
    bmc-boot-done \
    "

