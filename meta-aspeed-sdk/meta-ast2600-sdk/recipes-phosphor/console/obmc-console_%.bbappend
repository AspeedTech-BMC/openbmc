FILESEXTRAPATHS:append := ":${THISDIR}/${PN}"

# Declare port spcific config files
OBMC_CONSOLE_TTYS = "ttyS2"
CONSOLE_CLIENT = "2200"

CONSOLE_SERVER_CONF_FMT = "file://server.{0}.conf"
CONSOLE_CLIENT_CONF_FMT = "file://client.{0}.conf"
CONSOLE_CLIENT_SERVICE_FMT = "obmc-console-ssh@{0}.service"

SRC_URI += " \
             ${@compose_list(d, 'CONSOLE_SERVER_CONF_FMT', 'OBMC_CONSOLE_TTYS')} \
             ${@compose_list(d, 'CONSOLE_CLIENT_CONF_FMT', 'CONSOLE_CLIENT')} \
           "

SYSTEMD_SERVICE:${PN}:append = " \
                                  ${@compose_list(d, 'CONSOLE_CLIENT_SERVICE_FMT', 'CONSOLE_CLIENT')} \
                                "
SYSTEMD_SERVICE:${PN}:remove = "obmc-console-ssh.socket"

FILES:${PN}:remove = "${systemd_system_unitdir}/obmc-console-ssh@.service.d/use-socket.conf"

PACKAGECONFIG:append = " concurrent-servers"

do_install:append() {
    # Remove OpenBMC obmc-console default rules
    rm -rf ${D}${nonarch_base_libdir}/udev/rules.d/80-obmc-console-uart.rules
    # Install the console client configurations
    install -m 0644 ${UNPACKDIR}/client.*.conf ${D}${sysconfdir}/${BPN}/
}
