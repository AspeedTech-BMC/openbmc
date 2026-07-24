FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:aspeed-g6 = " \
                 file://0001-change-pre-sensor-scaling-to-2.5v.patch \
                 "
SRC_URI:append:aspeed-g7 = " \
                 file://0001-change-pre-sensor-scaling-to-2.5v.patch \
                 "

# Install only the required dbus-sensors to reduce the size of the image-rofs.
PACKAGECONFIG = "adcsensor"
PACKAGECONFIG:append = " fansensor"
PACKAGECONFIG:append = " hwmontempsensor"
PACKAGECONFIG:append = " intrusionsensor"
