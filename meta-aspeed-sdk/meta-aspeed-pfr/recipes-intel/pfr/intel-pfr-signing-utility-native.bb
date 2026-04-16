SUMMARY = "Intel(R) Platform Firmware Resilience Signing Utility"
DESCRIPTION = "Image signing tool for building Intel(R) PFR image"

inherit pkgconfig cmake native

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING.apache-2.0;md5=34400b68072d710fecd0a2940a0d1658"

DEPENDS = "openssl-native libxml2-native hash-sigs-native "

SRC_URI = "git://github.com/Intel-BMC/intel-pfr-signing-utility;protocol=https;branch=master \
           file://0001-support-openssl-3.0.patch \
           file://0002-fix-verify-error-if-block1-b0sig-hashalg-set-to-sha384.patch \
           file://0003-Fix-signature-RS-extration-error.patch \
           file://0004-To-add-LMS-support-utility.patch \
           file://0005-Add-header-file-path-of-hash-sigs-package.patch \
          "

SRCREV = "2c6f15434db57e5f51e3b1a4817f0e621a5bad25"


do_install:append() {
   install -d ${D}/${bindir}
   install -m 775 ${B}/intel-pfr-signing-utility* ${D}/${bindir}
}

EXTRA_OECMAKE:append = ' -DYOCTO_STAGING_INCDIR_NATIVE="${STAGING_INCDIR_NATIVE}" '
# Fix CMake Error: Compatibility with CMake < 3.5 has been removed from CMake
EXTRA_OECMAKE:append = " -DCMAKE_POLICY_VERSION_MINIMUM=3.5"
