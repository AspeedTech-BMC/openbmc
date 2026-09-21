FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
        file://0001-mctp-add-ast2700-i3c-target-network-transport.patch \
        file://ast2700-dcscm.cfg \
        file://ast2700-dcscm-mctp-socket.dts \
        file://ast2700a1-dcscm-mctp-socket.dts \
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
