KBRANCH = "aspeed-master-v5.4"
LINUX_VERSION ?= "5.4.173"

# Tag for v00.04.24
SRCREV = "60d0d602b2850048e4f9c4f091c515ae4b72803b"

require linux-aspeed.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

DEPENDS += "lzop-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
