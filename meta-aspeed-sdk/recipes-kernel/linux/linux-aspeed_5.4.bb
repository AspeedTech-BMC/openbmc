KBRANCH = "aspeed-master-v5.4"
LINUX_VERSION ?= "5.4"

# Tag for v00.04.27
SRCREV = "3eadfdbf8f3705dc5b2e10357d85369218019e36"

require linux-aspeed.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

DEPENDS += "lzop-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
