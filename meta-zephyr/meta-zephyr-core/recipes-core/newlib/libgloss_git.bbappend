FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:qemu-x86 = " file://0001-libgloss-i386-Disable-warnings-as-errors.patch"
