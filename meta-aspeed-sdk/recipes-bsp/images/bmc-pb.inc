SUMMARY = "ASPEED BMC Prebuilt Binaries"
HOMEPAGE = "https://github.com/AspeedTech-BMC/bmc-pb"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

BRANCH = "master"
SRC_URI = "git://gerrit.aspeed.com:29418/bmc-pb.git;protocol=ssh;branch=${BRANCH}"

SRCREV = "${AUTOREV}"

PV = "1.0+git"
S = "${WORKDIR}/git"
