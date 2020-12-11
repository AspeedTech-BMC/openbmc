HOMEPAGE = "https://github.com/AspeedTech-BMC/u-boot"
SECTION = "bootloaders"
DEPENDS += "flex-native bison-native"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=30503fd321432fc713238f582193b78e"
PE = "1"

# We use the revision in order to avoid having to fetch it from the
# repo during parse
#openbmc github
#SRC_URI = "git://git@github.com/openbmc/u-boot.git;nobranch=1;protocol=https"
#SRCREV = "0991f0b8ae2eaad7d06977d2fabbb599f0d28a97"

#aspeed github
UBRANCH = "aspeed-master-v2019.04"
SRCREV = "6d6e9c942ccb635b4520ecb6ab11e89386ebc674"
SRC_URI = "git://github.com/AspeedTech-BMC/u-boot;branch=${UBRANCH};protocol=https"


S = "${WORKDIR}/git"

PV = "v2019.04+git${SRCPV}"
