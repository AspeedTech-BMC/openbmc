SUMMARY = "OpenBMC for AspeedTech - System"

PR = "r1"

inherit packagegroup

PROVIDES = "${PACKAGES}"
RPROVIDES:${PN} = "${PACKAGES}"

PACKAGES = " \
    ${PN}-chassis \
    ${PN}-extras \
    ${PN}-fans \
    ${PN}-flash \
    ${PN}-system \
    ${PN}-inband \
    ${PN}-apps \
    "

PROVIDES:append = " virtual/obmc-chassis-mgmt"
PROVIDES:append = " virtual/obmc-fan-mgmt"
PROVIDES:append = " virtual/obmc-flash-mgmt"
PROVIDES:append = " virtual/obmc-system-mgmt"

RPROVIDES:${PN}-chassis = " virtual-obmc-chassis-mgmt"
RPROVIDES:${PN}-fans = " virtual-obmc-fan-mgmt"
RPROVIDES:${PN}-flash = " virtual-obmc-flash-mgmt"
RPROVIDES:${PN}-system = " virtual-obmc-system-mgmt"

SUMMARY:${PN}-chassis = "AspeedTech Chassis"
RDEPENDS:${PN}-chassis = " \
    x86-power-control \
    "

SUMMARY:${PN}-fans = "AspeedTech Fans"
RDEPENDS:${PN}-fans = " \
    phosphor-pid-control \
    "

SUMMARY:${PN}-flash = "AspeedTech Flash"
RDEPENDS:${PN}-flash = " \
    phosphor-software-manager \
    "

SUMMARY:${PN}-system = "AspeedTech System"
RDEPENDS:${PN}-system = " \
    phosphor-hostlogger \
    phosphor-sel-logger \
    phosphor-post-code-manager \
    phosphor-host-postd \
    "

SUMMARY:${PN}-inband = "AspeedTech Inband Test"
RDEPENDS:${PN}-inband = " \
    phosphor-ipmi-ipmb \
    phosphor-ipmi-ssif \
    phosphor-ipmi-bt \
    phosphor-ipmi-kcs \
    "

SUMMARY:${PN}-apps = "Open Source Applications for OpenBMC Image"
RDEPENDS:${PN}-apps = " \
    ipmitool \
    at-scale-debug \
    pdbg \
    "

# Only install in AST26xx and AST27xx series rofs as the free space of AST25xx rofs is not enough.
RDEPENDS:${PN}-apps:remove:aspeed-g5 = " \
    at-scale-debug \
    pdbg \
    "
