SUMMARY = "Set of tools to interact with i3c devices from user space"
HOMEPAGE = "https://github.com/vitor-soares-snps/i3c-tools"

LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://i3ctransfer.c;endline=6;md5=8a1ae5c1aaf128e640de497ceaa9935e"

SRC_URI = "git://gerrit.aspeed.com:29418/i3c-tools.git;protocol=ssh;branch=${BRANCH}"

PR = "r1"
PV = "1.0+git"
SRCREV = "${AUTOREV}"
BRANCH = "develop"


inherit meson

