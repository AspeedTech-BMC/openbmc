FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://ast2700-evb.json"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     # Remove upstream configuration JSON files so only the platform specific one is packaged.
     rm -rf ${D}${datadir}/entity-manager/configurations
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/ast2700-evb.json ${D}${datadir}/entity-manager/configurations/
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}
