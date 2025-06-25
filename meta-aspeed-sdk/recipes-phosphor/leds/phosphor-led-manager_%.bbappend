FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://ast2600-led-group-config.json"

PACKAGECONFIG:append = " use-json use-lamp-test"

do_install:append() {
        install -m 0644 ${UNPACKDIR}/ast2600-led-group-config.json \
            ${D}${datadir}/phosphor-led-manager/led-group-config.json
}

