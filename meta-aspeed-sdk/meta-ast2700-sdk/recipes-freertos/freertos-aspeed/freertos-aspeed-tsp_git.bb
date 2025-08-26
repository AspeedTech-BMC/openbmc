require freertos-aspeed-src.inc

SUMMARY = "The Tertiary Service Processor (TSP) FreeRTOS firmware"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/tsp"

FREERTOS_MACHINE = "${FREERTOS_TSP_MACHINE}"
IMG_SUFFIX = "tsp"

