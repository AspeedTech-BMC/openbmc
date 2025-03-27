require optee-os-helper.inc

do_deploy:append() {
    # install core in firmware
    install -m 644 ${B}/core/tee.dmp ${DEPLOYDIR}/${MLPREFIX}optee
    install -m 644 ${B}/core/tee.map ${DEPLOYDIR}/${MLPREFIX}optee
}
