FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-config-enhance-aspeed-uart-routing-for-dual-nodes.patch"

CONSOLE_CLIENT ?= "2200"

SYSTEMD_SERVICE:${PN}:remove = "obmc-console-ssh.socket"

FILES:${PN}:remove = "${systemd_system_unitdir}/obmc-console-ssh@.service.d/use-socket.conf"

PACKAGECONFIG:append = " concurrent-servers"

do_install:append() {
    # Remove OpenBMC obmc-console default rules
    rm -rf ${D}${nonarch_base_libdir}/udev/rules.d/80-obmc-console-uart.rules
    # Install the console client configurations
    install -m 0644 ${UNPACKDIR}/client.*.conf ${D}${sysconfdir}/${BPN}/

    # Add obmc-console service override to customize service behavior for each tty.
    for tty in ${OBMC_CONSOLE_TTYS}; do
        if [ -f ${UNPACKDIR}/override-${tty}.conf ]; then
            install -d ${D}${systemd_unitdir}/system/obmc-console@${tty}.service.d
            install -m 0644 ${UNPACKDIR}/override-${tty}.conf \
              ${D}${systemd_unitdir}/system/obmc-console@${tty}.service.d/override.conf
        fi
    done

    # Install the services in the multi-user.target
    install -m 0644 -d ${D}${systemd_unitdir}/system/multi-user.target.wants
    for port in ${CONSOLE_CLIENT}; do
        ln -s ../obmc-console-ssh@.service ${D}${systemd_unitdir}/system/multi-user.target.wants/obmc-console-ssh@${port}.service
    done
}
