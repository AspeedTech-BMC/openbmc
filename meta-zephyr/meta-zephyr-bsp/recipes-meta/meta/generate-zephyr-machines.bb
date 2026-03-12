# SPDX-FileCopyrightText: Huawei Inc.
# SPDX-License-Identifier: Apache-2.0
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

ZEPHYR_INHERIT_CLASSES += "zephyr cmake zephyr-sample"

inherit ${ZEPHYR_INHERIT_CLASSES}

SRC_URI:append = " file://0001-zephyr-Export-an-OpenEmbedded-machine-config.patch"

ZEPHYR_SRC_DIR = "${S}/samples/hello_world"

OECMAKE_SOURCEPATH = "${ZEPHYR_SRC_DIR}"

OECMAKE_GENERATOR_ARGS += "-c"

# This is a bad assumption, but for now, it's what we have to find the meta-oe directory
MACHINE_TUNINGS ?= "${COREBASE}/meta/conf/machine"

EXTRA_WEST += "-DCONFIG_OEMACHINE_EXPORTS=y -DMETA_OE_BASE:STRING='${MACHINE_TUNINGS}'"

DEPENDS += "west-native"

do_compile() {
    cd ${S}
    for machine in $(west boards);
    do
        bbnote "Generating $machine"
        rm -rf {B}/$machine
        west build -d ${B}/$machine --cmake-only -b $machine samples/hello_world -- \
        ${EXTRA_WEST}|| bbwarn "$machine machine def failed";
    done
}

do_deploy () {
    cd ${S}
    for machine in $(west boards);
    do
        oe_board_name=$(echo $machine|sed 's/_/-/g')
        bbnote "Copying ${machine} to ${DEPLOY_DIR}"
        install -D ${B}/$machine/$oe_board_name.conf ${DEPLOYDIR}/$oe_board_name.conf || bbwarn "No $oe_board_name.conf found. Skipping.";
    done
}
