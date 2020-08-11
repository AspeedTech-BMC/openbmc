HOMEPAGE = "https://github.com/AspeedTech-BMC/u-boot"
SECTION = "bootloaders"
DEPENDS += "flex-native bison-native"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=30503fd321432fc713238f582193b78e"
PE = "1"

# We use the revision in order to avoid having to fetch it from the
# repo during parse
### OpenBMC
#SRCREV = "09e0e88ee0e79e9fa967ac1fa03f589a5d7eface"
#SRC_URI = "git://git@github.com/openbmc/u-boot.git;nobranch=1;protocol=https"
### ASPEED SDK
UBRANCH = "aspeed-dev-v2019.04"
SRCREV = "e95e10afc085dd5de549468e4a161105782e1272"
SRC_URI = "git://github.com/AspeedTech-BMC/u-boot;branch=${UBRANCH};protocol=https"

S = "${WORKDIR}/git"

PV = "v2019.04+git${SRCPV}"
