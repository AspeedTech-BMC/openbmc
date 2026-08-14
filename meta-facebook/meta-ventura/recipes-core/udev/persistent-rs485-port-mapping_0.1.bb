SUMMARY = "Persistent RS485 port mapping"
DESCRIPTION = "Configure persistent RS485 port mapping"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit systemd

S = "${UNPACKDIR}"

RDEPENDS:${PN} += "bash"

FILES:${PN} += " \
    ${sysconfdir}/rs485-rules \
    ${libexecdir}/ventura \
    ${systemd_system_unitdir} \
    "

SRC_URI += " \
    file://update-rs485-rules.service \
    file://update-rs485-rules.sh \
    file://rmc-v1.rules \
    file://rmc-v1plus.rules \
    file://rackmond-override.conf \
    file://wait-rs485-devices.sh \
    "

SYSTEMD_SERVICE:${PN} = " \
    update-rs485-rules.service \
    "

do_install() {
    VENTURA_LIBEXECDIR="${D}${libexecdir}/ventura"
    install -d ${VENTURA_LIBEXECDIR}
    install -m 0755 ${UNPACKDIR}/update-rs485-rules.sh ${VENTURA_LIBEXECDIR}
    install -m 0755 ${UNPACKDIR}/wait-rs485-devices.sh ${VENTURA_LIBEXECDIR}

    install -d ${D}${systemd_system_unitdir}/rackmond.service.d
    install -m 0644 ${UNPACKDIR}/rackmond-override.conf \
        ${D}${systemd_system_unitdir}/rackmond.service.d/rackmond-override.conf

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/update-rs485-rules.service ${D}${systemd_system_unitdir}

    install -d ${D}${sysconfdir}/rs485-rules
    # Dynamically install all *.rules files
    for rule in ${S}/*.rules; do
        if [ -f "$rule" ]; then
            install -m 0644 "$rule" ${D}${sysconfdir}/rs485-rules/
        fi
    done
}
