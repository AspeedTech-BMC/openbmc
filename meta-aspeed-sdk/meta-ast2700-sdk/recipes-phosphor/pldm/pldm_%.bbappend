FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast-irot = " file://host_eid "

do_install:append:ast-irot() {
    install -D -m 0644 ${UNPACKDIR}/host_eid ${D}/usr/share/pldm
    install -D -m 0755 ${S}/tools/fw-update/pldm_fwup_pkg_creator.py \
        ${D}${datadir}/pldm/pldm_fwup_pkg_creator.py
}
