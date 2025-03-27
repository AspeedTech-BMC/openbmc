# KCS0/1/2/3: Host0 LPC-KCS
# KCS8/9/10/11: Host0 PCIe-KCS
# KCS12/13/14/15: Host1 PCIe-KCS

KCS_DEVICE = " \
    ipmi-kcs0 \
    ipmi-kcs1 \
    ipmi-kcs2 \
    ipmi-kcs3 \
    ipmi-kcs8 \
    ipmi-kcs9 \
    ipmi-kcs10 \
    ipmi-kcs11 \
    ipmi-kcs12 \
    ipmi-kcs13 \
    ipmi-kcs14 \
    ipmi-kcs15 \
"

KCS_DEVICE:remove:ast2700-a0 = " \
    ipmi-kcs12 \
    ipmi-kcs13 \
    ipmi-kcs14 \
    ipmi-kcs15 \
"

SYSTEMD_SERVICE:${PN} = " \
    ${PN}@ipmi-kcs0.service \
    ${PN}@ipmi-kcs1.service \
    ${PN}@ipmi-kcs2.service \
    ${PN}@ipmi-kcs3.service \
    ${PN}@ipmi-kcs8.service \
    ${PN}@ipmi-kcs9.service \
    ${PN}@ipmi-kcs10.service \
    ${PN}@ipmi-kcs11.service \
    ${PN}@ipmi-kcs12.service \
    ${PN}@ipmi-kcs13.service \
    ${PN}@ipmi-kcs14.service \
    ${PN}@ipmi-kcs15.service \
"

SYSTEMD_SERVICE:${PN}:remove:ast2700-a0 = " \
    ${PN}@ipmi-kcs12.service \
    ${PN}@ipmi-kcs13.service \
    ${PN}@ipmi-kcs14.service \
    ${PN}@ipmi-kcs15.service \
"

