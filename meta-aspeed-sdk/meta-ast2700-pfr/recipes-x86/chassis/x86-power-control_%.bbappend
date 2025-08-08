FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

CONFIGFILE = "power-config-host0.json"

SRC_URI:append = " file://${CONFIGFILE}"

do_install:append() {
    install -d ${D}${datadir}/${PN}
    install -m 0644 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/${PN}/power-config-host0.json
}
