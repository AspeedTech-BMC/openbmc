FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

CONFIGFILE = "ast2700-dcscm.json"

SRC_URI:append = " file://${CONFIGFILE}"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     rm -f ${D}${datadir}/entity-manager/configurations/*.json
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}
