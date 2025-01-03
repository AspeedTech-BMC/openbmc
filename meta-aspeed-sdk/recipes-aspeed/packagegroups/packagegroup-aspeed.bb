SUMMARY = "AspeedTech BMC Package Group"

PR = "r2"

PACKAGE_ARCH="${TUNE_PKGARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"
RPROVIDES:${PN} = "${PACKAGES}"

PACKAGES = " \
    ${PN}-apps \
    ${PN}-crypto \
    ${PN}-ssif \
    ${PN}-mtdtest \
    ${PN}-ktools \
    ${PN}-usbtools \
    "

SUMMARY:${PN}-apps = "AspeedTech Test App"
RDEPENDS:${PN}-apps = " \
    aspeed-app \
    "

SUMMARY:${PN}-crypto = "AspeedTech Crypto"
RDEPENDS:${PN}-crypto = " \
    libcrypto \
    libssl \
    openssl \
    openssl-bin \
    openssl-conf \
    openssl-engines \
    "

SUMMARY:${PN}-ssif = "IPMI SMBus System Interface"
RDEPENDS:${PN}-ssif = " \
    "
RRECOMMENDS:${PN}-ssif= " \
    kernel-module-ipmi-msghandler \
    kernel-module-ipmi-ssif \
    kernel-module-ipmi-si \
    kernel-module-ipmi-devintf \
    "

SUMMARY:${PN}-mtdtest = "MTD test utility"
RDEPENDS:${PN}-mtdtest = " \
    "
RRECOMMENDS:${PN}-mtdtest= " \
    kernel-module-mtd-speedtest \
    kernel-module-mtd-stresstest \
    "

# The size of perf is 6MB
SUMMARY:${PN}-ktools = "kernel tools"
RDEPENDS:${PN}-ktools = " \
    "
RRECOMMENDS:${PN}-ktools= " \
    perf \
    "

SUMMARY:${PN}-usbtools = "USB test tools"
RDEPENDS:${PN}-usbtools = " \
    usb-gadget \
    "
