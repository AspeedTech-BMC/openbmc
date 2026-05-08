FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

CONFIGFILE = "ast2700-dcscm.json"

SRC_URI:append = " file://${CONFIGFILE}"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     # Remove upstream configuration JSON files so only the platform specific one is packaged.
     rm -fr ${D}${datadir}/entity-manager/configurations
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/entity-manager/configurations/
}
