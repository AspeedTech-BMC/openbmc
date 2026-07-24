FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

CONFIGFILE = "kenya.json"

SRC_URI:append = " file://${CONFIGFILE}"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     # Remove upstream configuration JSON files so only the platform specific one is packaged.
     rm -rf ${D}${datadir}/entity-manager/configurations
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/entity-manager/configurations/
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}
