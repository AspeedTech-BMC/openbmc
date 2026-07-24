LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://zephyr/LICENSE;md5=fa818a259cbed7ce8bc2a22d35a464fc"

CVE_PRODUCT = "zephyr"

inherit cmake

# This file might be included from other places (like other layers) and not
# having an explicit path to the patches directory, will make bitbake fail to
# find the patch(es) in SRC_URI.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

require recipes-kernel/zephyr-kernel/zephyr-kernel-src-${PREFERRED_VERSION_zephyr-kernel}.inc

# The patch to fix the issue of gen_kobject_list.py not being found is required
# for Zephyr 4.2.0 and above, but it doesn't apply to Zephyr 4.1.0 and below version,
# so we conditionally add it to the SRC_URI based on the version of zephyr-kernel
# being built.
SRC_URI:append = "\
    ${@'file://0001-fix-unable-find-gen_kobject_list.py.patch;patchdir=zephyr' if bb.utils.vercmp_string(d.getVar('PREFERRED_VERSION_zephyr-kernel'), '4.2.0') >= 0 else ''} \
"

# python3-jsonschema-native is required starting from Zephyr 4.3.0
DEPENDS += "\
    ${@'python3-jsonschema-native' if bb.utils.vercmp_string(d.getVar('PREFERRED_VERSION_zephyr-kernel'), '4.3.0') >= 0 else ''} \
    "
