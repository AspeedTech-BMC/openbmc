SUMMARY = "TSI BMC OpenBMC image"
DESCRIPTION = "Phosphor OpenBMC image for TSI BMC machines. Stage 1 mirrors \
the reference obmc-phosphor-image feature set for boot parity on QEMU; Stage 2 \
trims this down to a minimal out-of-band (OOB) image via packagegroup-tsi-oob."

# Reuse the upstream phosphor image definition so the TSI image is
# feature-equivalent to the reference for Stage 1. Resolved via BBPATH
# (meta-phosphor). Stage 2 will diverge from this baseline.
require recipes-phosphor/images/obmc-phosphor-image.bb
