KBRANCH = "aspeed-dev-v5.4"
LINUX_VERSION ?= "5.4.173"

SRCREV = "${AUTOREV}"

require linux-aspeed.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

DEPENDS += "lzop-native"
DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'ast-secure', 'aspeed-secure-config-native', '', d)}"

SRC_URI:append = " file://ipmi_ssif.cfg "
SRC_URI:append = " file://mtd_test.cfg "
SRC_URI:append = " file://init_disassemble_info-signature-changes-causes-compile-failures.patch "
SRC_URI:append = " file://0001-perf-parse-events-Disable-a-subset-of-flex-warnings.patch "
SRC_URI:append = " file://0002-perf-parse-events-Disable-a-subset-of-bison-warnings.patch "
SRC_URI:append = " file://0003-perf-parse-events-Fix-an-incompatible-pointer.patch "
SRC_URI:append = " file://0004-perf-parse-Add-struct-parse_events_state-pointer-to.patch "
SRC_URI:append = " file://0005-perf-tools-Add-an-option-to-build-without-libbfd.patch "
