# meta-tsi-bmc/recipes-phosphor/configuration/entity-manager_%.bbappend
#
# Entity Manager board configuration for the TSISIM machine.
#
# dbus-sensors daemons do not read hwmon on their own: hwmontempsensor waits
# for an xyz.openbmc_project.Configuration.* object naming the bus and address,
# and Entity Manager is what publishes those from the JSON installed here.
# Without a config the daemon starts and exposes nothing, so no sensor reaches
# D-Bus, bmcweb or Redfish.
#
# Only tsi-bmc-tsisim gets a config: it is the one machine whose I2C topology
# is known (a single tmp105 at 0-0048, attached by runner_bmc.sh through
# BMC_I2C_DEVICES and described in the device tree as ti,tmp75). The other
# tsi-bmc-* machines have no I2C model yet.

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tsi-bmc-tsisim = " file://tsi-bmc-tsisim.json"

# meta-aspeed-sdk ships its own entity-manager bbappend that installs
# ast2700-evb.json for every machine using that layer -- including ours, since
# tsi-bmc-common.inc pulls in the AST2700 SDK as a Stage 1 BSP stand-in. That
# config describes AST2700 EVB hardware that does not exist on TSISIM, so it is
# cleared here rather than left to probe against nothing. meta-tsi-bmc has
# BBFILE_PRIORITY 10 against the SDK layer's default, so this append runs last.
do_install:append:tsi-bmc-tsisim() {
    rm -f ${D}${datadir}/entity-manager/configurations/*.json
    install -d ${D}${datadir}/entity-manager/configurations
    install -m 0444 ${WORKDIR}/tsi-bmc-tsisim.json \
        ${D}${datadir}/entity-manager/configurations/
}
