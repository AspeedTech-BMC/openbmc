# ToDo: porting below tools for AMD platform
RDEPENDS:${PN}-apps:remove = " \
    pfr-mctp-i3c \
    "

# Add below apps for AMD platform.
RDEPENDS:${PN}-apps:append = " \
    set-fan-speed \
    amd-platform-init \
    "
