FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://0001-Add-back-maximum-transfer-size-configuration.patch \
"

SRC_URI:append:ast-irot = " file://host_eid "

PACKAGECONFIG:append:ast-irot = " fw-update-pkg-inotify "

EXTRA_OEMESON:append:ast-irot = " \
    -Dmaximum-transfer-size=32768 \
"

do_install:append:ast-irot() {
    install -D -m 0644 ${UNPACKDIR}/host_eid ${D}/usr/share/pldm
    install -D -m 0755 ${S}/tools/fw-update/pldm_fwup_pkg_creator.py \
        ${D}${datadir}/pldm/pldm_fwup_pkg_creator.py
}
