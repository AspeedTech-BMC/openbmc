FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://0001-libocp-add-OCP-Secure-Firmware-Recovery-protocol-lib.patch \
    file://0002-libocp-add-standalone-ocp-recovery-tool.patch \
    file://0003-libocp-add-I3CSysfsTransport.patch \
    file://0004-ocp-recovery-tool-add-i3c-support.patch \
    file://0005-libocp-add-INDIRECT_FIFO_CTRL-STATUS-DATA-v1.1-FIFO-.patch \
    file://0006-libocp-writeImageFifo-drop-FIFO-status-polling-rely-.patch \
    file://0007-libocp-fix-FIFO-chunk-sizing-power-of-two-128-byte-c.patch \
    file://0008-confirm-DEVICE_STATUS-recoveryPending-before-activat.patch \
    file://0009-ocp-recovery-tool-report-last-RECOVERY_STATUS-on-Per.patch \
    file://0010-confirm-RECOVERY_STATUS-awaitingImage-between-multi-.patch \
"

# For OCP Secure Firmware Recovery CLI tool
PACKAGECONFIG:append = " ocp-recovery-tool"

PACKAGECONFIG[ocp-recovery-tool] = "-Docp-recovery-tool=enabled, -Docp-recovery-tool=disabled, cli11"

FILES:${PN} += "${bindir}/ocp-recovery-tool"
