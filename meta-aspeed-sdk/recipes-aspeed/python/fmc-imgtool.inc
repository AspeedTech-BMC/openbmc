SUMMARY = "ASPEED FMC tool and images"
HOMEPAGE = "https://github.com/AspeedTech-BMC/fmc_imgtool"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=522c9891674011c4a2390d828772384d"

BRANCH = "develop"
SRC_URI = "git://gerrit.aspeed.com:29418/fmc_imgtool.git;protocol=ssh;branch=${BRANCH}"

SRCREV = "${AUTOREV}"

PV = "1.0+git"
S = "${WORKDIR}/git"
