FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://v1-0001-lib-ecdsa-Add-support-for-loading-ECDSA-pubkey.patch \
    "
