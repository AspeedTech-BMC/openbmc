FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

EM_MACHINE_CONF ?= "ast2700-evb.json"
EM_MACHINE_CONF:ast27x5 = "ast2705-evb.json"
EM_MACHINE_CONF:ast-irot = "ast2700-irot.json"

SRC_URI:append = " file://${EM_MACHINE_CONF}"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     # Remove upstream configuration JSON files so only the platform specific one is packaged.
     rm -rf ${D}${datadir}/entity-manager/configurations
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/${EM_MACHINE_CONF} ${D}${datadir}/entity-manager/configurations/
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}
