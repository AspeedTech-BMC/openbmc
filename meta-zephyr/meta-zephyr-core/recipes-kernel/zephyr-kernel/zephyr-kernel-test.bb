require recipes-kernel/zephyr-kernel/zephyr-image.inc
require recipes-kernel/zephyr-kernel/zephyr-kernel-test-${PREFERRED_VERSION_zephyr-kernel}.inc

BBCLASSEXTEND = '${@" ".join(["zephyrtest:" + x for x in d.getVar("ZEPHYRTESTS", True).split()])}'
