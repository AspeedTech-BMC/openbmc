FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:aspeed-g6 = " \
                 file://0001-change-pre-sensor-scaling-to-2.5v.patch \
                 file://0003-fansensor-update-regular-expression-to-find-pwm.patch \
                 "
SRC_URI:append:aspeed-g7 = " \
                 file://0001-change-pre-sensor-scaling-to-2.5v.patch \
                 file://0002-fansensor-support-ast2700-pwm-driver.patch \
                 file://0003-fansensor-update-regular-expression-to-find-pwm.patch \
                 "

# Only Linux-5.4 machine require this patch to fix timer problem
SRC_URI:append:ast2600-default-54 = " file://0004-linux-5.4-remove-BOOST_ASIO_DISABLE_EPOLL-to-fix-tim.patch"
SRC_URI:append:ast2500-default-54 = " file://0004-linux-5.4-remove-BOOST_ASIO_DISABLE_EPOLL-to-fix-tim.patch"

# Install only the required dbus-sensors to reduce the size of the image-rofs.
PACKAGECONFIG = "adcsensor"
PACKAGECONFIG:append = " fansensor"
PACKAGECONFIG:append = " hwmontempsensor"
PACKAGECONFIG:append = " intrusionsensor"
