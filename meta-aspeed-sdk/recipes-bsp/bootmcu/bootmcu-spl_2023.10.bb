require bootmcu-spl.inc
require recipes-bsp/u-boot/u-boot-common-aspeed-sdk_${PV}.inc

SRC_URI += "file://0001-scripts-dtc-pylibfdt-libfdt-i_shipped-Use-SWIG_AppendOutp.patch"
