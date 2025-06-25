FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://${PFR_MANIFEST}"

do_install:append:class-native() {
    install -d ${D}/${datadir}/pfrconfig
    install -m 400 ${UNPACKDIR}/${PFR_MANIFEST} ${D}/${datadir}/pfrconfig
}
