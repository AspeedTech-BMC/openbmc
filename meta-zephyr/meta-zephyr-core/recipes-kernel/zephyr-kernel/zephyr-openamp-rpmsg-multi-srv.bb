SUMMARY = "Open AMP rpmsg multi services"
DESCRIPTION = "Open AMP rpmsg multi services"

inherit zephyr-sample

SRC_URI_ZEPHYR_OPEN_AMP_SYS_REF ?= "git://github.com/OpenAMP/openamp-system-reference;protocol=https"

BRANCH ?= "v2024.05"
BRANCHARG = "${@['nobranch=1', 'branch=${BRANCH}'][d.getVar('BRANCH', True) != '']}"

SRC_URI:append = " \
    ${SRC_URI_ZEPHYR_OPEN_AMP_SYS_REF};name=open-amp-sys-ref;${BRANCHARG};destsuffix=${P}/open-amp-sys-ref \
    "

SRCREV_open-amp-sys-ref = "d78315763fbacba8a74552d0ad570bd01c42ccf9"

COMPATIBLE_MACHINE = "(qemu-cortex-r5|stm32mp157c-dk2)"

ZEPHYR_SRC_DIR = "${ZEPHYR_BASE}/../open-amp-sys-ref/examples/zephyr/rpmsg_multi_services"

ZEPHYR_MAKE_OUTPUT = "rpmsg_multi_services.elf"

EXTRA_OECMAKE += " \
    -DCONF_FILE="prj.conf" \
    "
