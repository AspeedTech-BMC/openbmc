FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
CONFIGFILE = "${@bb.utils.contains('MACHINE', 'ast2700-vff', \
                    'ast2700-vff.json', 'ast2700-evb.json', d)}"

SRC_URI:append = " file://${CONFIGFILE}"
SRC_URI:append = " file://blacklist.json"

do_install:append() {
     rm -f ${D}${datadir}/entity-manager/configurations/*.json
     install -d ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/entity-manager/configurations
     install -m 0444 ${UNPACKDIR}/blacklist.json -D -t ${D}${datadir}/entity-manager
}

# Add nostamp to avoid build failure when the machine changes.
do_configure[nostamp] = "1"
do_compile[nostamp] = "1"
do_install[nostamp] = "1"
