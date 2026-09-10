FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://0001-Add-back-maximum-transfer-size-configuration.patch \
"

SRC_URI:append:ast-irot = " \
    file://host_eid \
    file://0002-fw-update-send-apply-phase-progress-heartbeat.patch \
"

# Backport of PLDM firmware update package format revision 1.3.0, and the empty
# pre/post update condition config that the backport looks for at startup.
# This applies to every machine; on ast-irot it goes on top of 0002. Drop this
# once the SDK moves to an OpenBMC revision that already carries Gerrit 93835.
SRC_URI:append = " \
    file://0003-fw-update-support-package-format-revision-1.3.0.patch \
    file://fw-update-targets.json \
"

EXTRA_OEMESON:append:ast-irot = " \
    -Dmaximum-transfer-size=32768 \
"

EXTRA_OEMESON:append = " \
    -Dfw-update-targets-json=${datadir}/pldm/fw-update-targets.json \
"

do_install:append:ast-irot() {
    install -D -m 0644 ${UNPACKDIR}/host_eid ${D}/usr/share/pldm
    install -D -m 0755 ${S}/tools/fw-update/pldm_fwup_pkg_creator.py \
        ${D}${datadir}/pldm/pldm_fwup_pkg_creator.py
}

do_install:append() {
    install -D -m 0644 ${UNPACKDIR}/fw-update-targets.json \
        ${D}${datadir}/pldm/fw-update-targets.json
}
