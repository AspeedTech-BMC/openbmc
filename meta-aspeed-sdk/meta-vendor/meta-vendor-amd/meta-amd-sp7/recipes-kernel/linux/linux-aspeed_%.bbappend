FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
        file://ast2700-amd-dcscm.cfg \
        file://aspeed-bmc-amd-kenya.dts \
"

do_prepare_dts() {
    for DTB in ${KERNEL_DEVICETREE}; do
        DT=`basename ${DTB} .dtb`
        if [ -r "${UNPACKDIR}/${DT}.dts" ]; then
            cp ${UNPACKDIR}/${DT}.dts \
                ${STAGING_KERNEL_DIR}/arch/${ARCH}/boot/dts/aspeed/
        fi
    done
}

addtask prepare_dts before do_configure after do_set_local_version
