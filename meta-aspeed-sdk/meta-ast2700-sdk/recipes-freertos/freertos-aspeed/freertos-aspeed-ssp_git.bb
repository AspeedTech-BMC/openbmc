require freertos-aspeed-src.inc

SUMMARY = "The Secondary Service Processor (SSP) FreeRTOS firmware"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/ssp"

FREERTOS_MACHINE = "${FREERTOS_SSP_MACHINE}"
IMG_SUFFIX = "ssp"

