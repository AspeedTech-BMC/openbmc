FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = "  file://ast2600-dcscm.json"
SRC_URI:append = "  file://blacklist.json"

do_install:append() {
     rm -f ${D}${datadir}/entity-manager/configurations/*.json
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/ast2600-dcscm.json ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}
