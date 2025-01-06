SUMMARY = "Packagegroup for Open Source"

PR = "r1"

PACKAGE_ARCH="${TUNE_PKGARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"

PACKAGES = " \
    ${PN}-apps \
    ${PN}-libs \
    ${PN}-intel-pmci \
    ${PN}-extended \
    ${PN}-extra \
    "

# The size of fio is very large because its dependencies includes python3-core.
# The size of fio and python3-core are 10MB.
# A nvme-cli recipe creates post installation script.
# It caused that generated read-only file system failed.
# The root file system is read-only for emmc boot.
# To fix it, adds "read-only-rootfs-delayed-postinsts" in IMAGE_FEATURES for emmc boot.
# https://github.com/AspeedTech-BMC/openbmc/blob/aspeed-master/meta-openembedded/meta-oe/recipes-bsp/nvme-cli/nvme-cli_2.6.bb
SUMMARY:${PN}-apps = "Open Source Applications"
RDEPENDS:${PN}-apps = " \
    pievo-mdio-tool \
    mdio-tools \
    gperf \
    iperf3 \
    pciutils \
    ethtool \
    mmc-utils \
    i3c-tools \
    i2c-tools \
    xdma-test \
    libpeci \
    dhrystone \
    nbd-client \
    iozone3 \
    ncsi-netlink \
    hdparm \
    stressapptest \
    e2fsprogs-mke2fs \
    nvme-cli \
    ${@d.getVar('PREFERRED_PROVIDER_u-boot-fw-utils', True) or 'u-boot-fw-utils'} \
    aer-inject \
    fio \
    mctp \
    memtester \
    coremark \
    "

# The size of opkg package manager is 6XX KB.
SUMMARY:${PN}-extended = "Open Source Extended Applications"
RDEPENDS:${PN}-extended = " \
    can-utils \
    opkg \
    "

SUMMARY:${PN}-extra = "Open Source Extra Applications"
RDEPENDS:${PN}-extra = " \
    freeipmi \
    freeipmi-ipmi-raw \
    "

SUMMARY:${PN}-intel-pmci = "Open Source Intel PMCI Applications"
RDEPENDS:${PN}-intel-pmci = " \
    libmctp-intel-test \
    "

# Only install in AST26xx and AST27xx series rofs as the free space of AST25xx rofs is not enough.
RDEPENDS:${PN}-apps:remove:aspeed-g5 = " \
    mdio-tools \
    i3c-tools \
    iozone3 \
    hdparm \
    fio \
    coremark \
    memtester \
    aer-inject \
    pciutils \
    dhrystone \
    nvme-cli \
    "

SUMMARY:${PN}-libs = "Open Source Library"
RDEPENDS:${PN}-libs = " \
    libgpiod \
    libgpiod-tools \
    "
