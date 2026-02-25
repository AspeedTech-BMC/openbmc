SUMMARY = "ASPEED BMC Prebuilt Binaries"
HOMEPAGE = "https://github.com/AspeedTech-BMC/bmc-pb"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

BRANCH = "master"
SRC_URI = "git://github.com/AspeedTech-BMC/bmc-pb.git;protocol=https;branch=${BRANCH}"

# Tag for v00.01.00
SRCREV = "5531ad74a717b5e62c0f1648a608637786b5b3fa"

PV = "1.0+git"
S = "${WORKDIR}/git"
