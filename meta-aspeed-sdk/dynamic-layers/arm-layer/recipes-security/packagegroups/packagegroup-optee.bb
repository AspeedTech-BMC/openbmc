SUMMARY = "Packagegroup for optee"

PR = "r1"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"

PACKAGES = " \
    ${PN}-apps \
    "

SUMMARY:${PN}-apps = "Optee Applications"
RDEPENDS:${PN}-apps = " \
    optee-client \
    optee-examples \
    "
