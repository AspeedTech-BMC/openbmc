FILESEXTRAPATHS:append := ":${THISDIR}/${PN}"

# Declare port spcific config files
OBMC_CONSOLE_TTYS = "ttyS2 ttyS7"
CONSOLE_CLIENT = "2200 2201"

CONSOLE_SERVER_CONF_FMT = "file://server.{0}.conf"
CONSOLE_CLIENT_CONF_FMT = "file://client.{0}.conf"
CONSOLE_CLIENT_SERVICE_FMT = "obmc-console-ssh@{0}.service"

SRC_URI += " \
             ${@compose_list(d, 'CONSOLE_SERVER_CONF_FMT', 'OBMC_CONSOLE_TTYS')} \
             ${@compose_list(d, 'CONSOLE_CLIENT_CONF_FMT', 'CONSOLE_CLIENT')} \
             file://override-ttyS2.conf \
             file://override-ttyS7.conf \
           "
