KCS_DEVICE = " \
    ipmi-kcs2 \
    "

SYSTEMD_SERVICE:${PN} = " \
    ${PN}@ipmi-kcs2.service \
    "
