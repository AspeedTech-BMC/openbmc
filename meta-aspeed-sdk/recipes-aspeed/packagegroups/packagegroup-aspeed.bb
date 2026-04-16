SUMMARY = "AspeedTech BMC Package Group"

PR = "r2"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

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
    ast-crypto-engine \
    "

SUMMARY:${PN}-ssif = "IPMI SMBus System Interface"
RDEPENDS:${PN}-ssif = " \
    "
RRECOMMENDS:${PN}-ssif = " \
    kernel-module-ipmi-msghandler \
    kernel-module-ipmi-ssif \
    kernel-module-ipmi-si \
    kernel-module-ipmi-devintf \
    "

SUMMARY:${PN}-mtdtest = "MTD test utility"
RDEPENDS:${PN}-mtdtest = " \
    "
RRECOMMENDS:${PN}-mtdtest = " \
    kernel-module-mtd-speedtest \
    kernel-module-mtd-stresstest \
    "

# The size of perf is 6MB
# Skip perf for Linux-5.15 due to build failure: missing 'install_headers' target
# in tools/lib/api/Makefile. To build perf on Linux 5.15,
# users can backport the install_headers target from Linux-6.6.
SUMMARY:${PN}-ktools = "kernel tools"
RDEPENDS:${PN}-ktools = " \
    "
RRECOMMENDS:${PN}-ktools = " \
    ${@'' if (d.getVar('PREFERRED_VERSION_linux-aspeed') or '').startswith('5.15') else 'perf'} \
    "

SUMMARY:${PN}-usbtools = "USB test tools"
RDEPENDS:${PN}-usbtools = " \
    usb-gadget \
    "
