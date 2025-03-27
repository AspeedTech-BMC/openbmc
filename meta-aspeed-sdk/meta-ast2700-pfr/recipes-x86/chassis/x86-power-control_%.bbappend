FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

CONFIGFILE = "${@bb.utils.contains('MACHINE_FEATURES', 'ast2700-a0', \
                'power-config-host0_a0.json', 'power-config-host0.json', d)}"

SRC_URI:append = " file://${CONFIGFILE}"

# AST2700 A0 doesn't support SGPIO Slave interrupt.
# If the SGPIOS input pin is configured for x86-power-control PowerOk,
# the power status will not update.
# We use phosphor-inventory-manager to create DBus,
# We need to wait power-status-sync.service start running and update SGPIO value to DBus.
DEPS_CFG = "power.conf"
DEPS_TGT = "xyz.openbmc_project.Chassis.Control.Power@.service"
SYSTEMD_OVERRIDE:${PN}:append:ast2700-a0 = "${DEPS_CFG}:${DEPS_TGT}.d/${DEPS_CFG}"

do_install:append() {
    install -d ${D}${datadir}/${PN}
    install -m 0644 ${UNPACKDIR}/${CONFIGFILE} ${D}${datadir}/${PN}/power-config-host0.json
}

# Add nostamp to avoid build failure when the machine changes from ast2700-a0 to a1.
do_configure[nostamp] = "1"
do_compile[nostamp] = "1"
do_install[nostamp] = "1"
