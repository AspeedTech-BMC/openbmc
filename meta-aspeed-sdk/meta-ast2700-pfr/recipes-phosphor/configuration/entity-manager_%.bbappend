FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

CONFIGFILE = "${@bb.utils.contains('MACHINE_FEATURES', 'ast2700-a0', \
                'ast2700a0-dcscm.json', 'ast2700-dcscm.json', d)}"

SRC_URI:append = " file://${CONFIGFILE}"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     rm -f ${D}${datadir}/entity-manager/configurations/*.json
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}

# Add nostamp to avoid build failure when the machine changes from ast2700-a0 to a1.
do_configure[nostamp] = "1"
do_compile[nostamp] = "1"
do_install[nostamp] = "1"
