FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:ast-irot = " \
                           file://0001-spdmd-add-BMC-mediated-composite-attestation-support.patch \
                           file://0002-nv-spdm-fix-compiling-error-with-composite-eat-enabl.patch \
                           file://0003-composite_eat-add-irot-as-backend.patch \
                           file://0004-spdm-composite-eat-change-schedule-and-workaound.patch \
                           file://spdmd_conf.json \
                           file://composite.json \
"
EXTRA_OEMESON:append:ast-irot = " \
    -Dsystemd=enabled \
    -Dcomposite-attestation=enabled \
    -Dattester-backend=irot \
    -Dfetch_serialnumber_from_responder=0 \
"
DEPENDS:append:ast-irot = " \
    tinycbor \
"
SYSTEMD_SERVICE:${PN}:ast-irot = "spdmd.service"

do_install:append:ast-irot() {
    install -d ${D}${sysconfdir}
    install -d ${D}${sysconfdir}/spdmd
    install -m 0644 ${UNPACKDIR}/composite.json ${D}${sysconfdir}/spdmd
    install -m 0644 ${UNPACKDIR}/spdmd_conf.json ${D}${sysconfdir}
    
}
