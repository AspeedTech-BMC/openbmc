KBRANCH = "aspeed-master-v5.4"
LINUX_VERSION ?= "5.4"

# Tag for v00.04.25
SRCREV = "389a553eb9fefa75d5dd9c8ed7875d0aa6ed3bb3"

require linux-aspeed.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

DEPENDS += "lzop-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
