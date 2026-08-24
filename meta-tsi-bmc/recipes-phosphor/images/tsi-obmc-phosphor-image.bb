SUMMARY = "TSI BMC OpenBMC image"
DESCRIPTION = "Phosphor OpenBMC image for TSI BMC machines. Stage 1 mirrors \
the reference obmc-phosphor-image feature set for boot parity on QEMU; Stage 2 \
trims this down to a minimal out-of-band (OOB) image via packagegroup-tsi-oob."

# Reuse the upstream phosphor image definition so the TSI image is
# feature-equivalent to the reference for Stage 1. Resolved via BBPATH
# (meta-phosphor). Stage 2 will diverge from this baseline.
require recipes-phosphor/images/obmc-phosphor-image.bb

# entity-manager is not pulled in by obmc-phosphor-image, nor by
# packagegroup-aspeed-obmc (the virtual/obmc-system-mgmt provider this machine
# uses), so it was absent from the image. That is not a cosmetic gap:
# xyz.openbmc_project.hwmontempsensor.service carries
#   Requires=xyz.openbmc_project.EntityManager.service
# so with entity-manager missing the unit fails to start outright and no
# dbus-sensors daemon publishes anything. Its board config for TSISIM comes
# from meta-tsi-bmc's entity-manager bbappend.
IMAGE_INSTALL:append = " entity-manager"
