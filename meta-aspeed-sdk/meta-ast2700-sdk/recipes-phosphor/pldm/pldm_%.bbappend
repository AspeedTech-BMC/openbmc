FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast-irot = " file://host_eid "

do_install:append:ast-irot() {
    install -D -m 0644 ${UNPACKDIR}/host_eid ${D}/usr/share/pldm
}
