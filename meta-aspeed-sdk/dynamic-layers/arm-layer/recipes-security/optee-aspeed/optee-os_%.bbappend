require optee-os-helper.inc

# The tee.dmp and tee.map files are for debugging.
do_deploy:append() {
    # install core in firmware
    install -m 644 ${B}/core/tee.dmp ${DEPLOYDIR}/${MLPREFIX}optee
    install -m 644 ${B}/core/tee.map ${DEPLOYDIR}/${MLPREFIX}optee
}