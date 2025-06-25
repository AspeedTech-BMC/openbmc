SUMMARY = "PFR image utils"
PR = "r1"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# 1 = SHA256
# 2 = SHA384
PFR_SHA ?= "1"
PFR_MANIFEST ?= "pfr_manifest_ast2600_dcscm.json"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI = " \
           file://pfr_image.py \
           file://pfm_config.xml \
           file://bmc_config.xml \
           file://csk_prv.pem \
           file://csk_pub.pem \
           file://rk_pub.pem \
           file://rk_prv.pem \
           file://rk_cert.pem \
           file://csk384_prv.pem \
           file://csk384_pub.pem \
           file://rk384_pub.pem \
           file://rk384_prv.pem \
           file://pfm_config_secp384r1.xml \
           file://bmc_config_secp384r1.xml \
           file://rootkey_h10.prv \
           file://rootkey_h10.pub \
           file://cskkey_h10.prv \
           file://cskkey_h10.pub \
           file://pfm_config_lms384.xml \
           file://bmc_config_lms384.xml \
           file://pfm_config_lms256.xml \
           file://bmc_config_lms256.xml \
          "

do_install () {
        install -d ${D}${bindir}
        install -d ${D}/${datadir}/pfrconfig
        install -m 775 ${UNPACKDIR}/pfr_image.py ${D}${bindir}/pfr_image.py
        install -m 400 ${UNPACKDIR}/pfm_config.xml ${D}/${datadir}/pfrconfig/pfm_config.xml
        install -m 400 ${UNPACKDIR}/bmc_config.xml ${D}/${datadir}/pfrconfig/bmc_config.xml
        install -m 400 ${UNPACKDIR}/csk_prv.pem ${D}/${datadir}/pfrconfig/csk_prv.pem
        install -m 400 ${UNPACKDIR}/csk_pub.pem ${D}/${datadir}/pfrconfig/csk_pub.pem
        install -m 400 ${UNPACKDIR}/rk_pub.pem ${D}/${datadir}/pfrconfig/rk_pub.pem
        install -m 400 ${UNPACKDIR}/rk_prv.pem ${D}/${datadir}/pfrconfig/rk_prv.pem
        install -m 0644 ${UNPACKDIR}/rk_cert.pem ${D}/${datadir}/pfrconfig/rk_cert.pem
        install -m 400 ${UNPACKDIR}/csk384_prv.pem ${D}/${datadir}/pfrconfig/csk384_prv.pem
        install -m 400 ${UNPACKDIR}/csk384_pub.pem ${D}/${datadir}/pfrconfig/csk384_pub.pem
        install -m 400 ${UNPACKDIR}/rk384_pub.pem ${D}/${datadir}/pfrconfig/rk384_pub.pem
        install -m 400 ${UNPACKDIR}/rk384_prv.pem ${D}/${datadir}/pfrconfig/rk384_prv.pem
        install -m 400 ${UNPACKDIR}/pfm_config_secp384r1.xml ${D}/${datadir}/pfrconfig/pfm_config_secp384r1.xml
        install -m 400 ${UNPACKDIR}/bmc_config_secp384r1.xml ${D}/${datadir}/pfrconfig/bmc_config_secp384r1.xml
        install -m 644 ${UNPACKDIR}/rootkey_h10.prv ${D}/${datadir}/pfrconfig/rootkey_h10.prv
        install -m 644 ${UNPACKDIR}/rootkey_h10.pub ${D}/${datadir}/pfrconfig/rootkey_h10.pub
        install -m 644 ${UNPACKDIR}/cskkey_h10.prv ${D}/${datadir}/pfrconfig/cskkey_h10.prv
        install -m 644 ${UNPACKDIR}/cskkey_h10.pub ${D}/${datadir}/pfrconfig/cskkey_h10.pub
        install -m 400 ${UNPACKDIR}/pfm_config_lms384.xml ${D}/${datadir}/pfrconfig/pfm_config_lms384.xml
        install -m 400 ${UNPACKDIR}/bmc_config_lms384.xml ${D}/${datadir}/pfrconfig/bmc_config_lms384.xml
        install -m 400 ${UNPACKDIR}/pfm_config_lms256.xml ${D}/${datadir}/pfrconfig/pfm_config_lms256.xml
        install -m 400 ${UNPACKDIR}/bmc_config_lms256.xml ${D}/${datadir}/pfrconfig/bmc_config_lms256.xml
}

do_install:class-target () {
        install -d ${D}/${datadir}/pfrconfig

        if [ "${PFR_SHA}" = "1" ]; then
                install -m 400 ${UNPACKDIR}/rk_pub.pem ${D}/${datadir}/pfrconfig/rk_pub.pem
        elif [ "${PFR_SHA}" = "2" ]; then
                install -m 400 ${UNPACKDIR}/rk384_pub.pem ${D}/${datadir}/pfrconfig/rk384_pub.pem
        else
                install -m 400 ${UNPACKDIR}/rootkey_h10.pub ${D}/${datadir}/pfrconfig/rootkey_h10.pub
        fi
}

FILES:${PN}:append = " ${datadir}/pfrconfig"

BBCLASSEXTEND = "native nativesdk"
