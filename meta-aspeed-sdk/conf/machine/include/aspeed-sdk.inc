PREFERRED_PROVIDER_virtual/kernel ?= "linux-aspeed"
PREFERRED_PROVIDER_virtual/bootloader ?= "u-boot-aspeed-sdk"
PREFERRED_PROVIDER_u-boot ?= "u-boot-aspeed-sdk"
PREFERRED_PROVIDER_u-boot-fw-utils ?= "u-boot-fw-utils-aspeed-sdk"

# Build a FIT image and stuff core-image-minimal in it by default.
KERNEL_IMAGETYPE ?= "fitImage"
KERNEL_CLASSES ?= "kernel-fitimage"
INITRAMFS_IMAGE ?= "aspeed-image-initramfs"
INITRAMFS_FSTYPES ?= "cpio.xz"

WKS_FILE ?= "ast2600-emmc.wks.in"
WKS_FILE_DEPENDS ?= " \
    phosphor-hostfw-image \
    virtual/bootloader \
    virtual/kernel \
"
WKS_HOSTFW_SIZE ?= "5G"
IMAGE_BOOT_FILES ?= "fitImage-${INITRAMFS_IMAGE}-${MACHINE}-${MACHINE};fitImage"

MACHINE_EXTRA_RRECOMMENDS += "udev-aspeed-vuart udev-aspeed-mtd-partitions"
MACHINE_FEATURES += "hw-rng"

MACHINEOVERRIDES =. "aspeed:"

PREFERRED_VERSION_iperf3 ?= "3.1.4"

# It should be removed if kernel 5.4 is no longer supported.
require oldest-kernel-54.inc

# Installs static development packages, which are static libraries (i.e. *.a files), for all packages installed in a given SDK
SDKIMAGE_FEATURES += "staticdev-pkgs"

# Set VIRTUAL-RUNTIME for obmc-fan-control and obmc-sensors-hwmon.
VIRTUAL-RUNTIME_obmc-fan-control = "phosphor-pid-control"
VIRTUAL-RUNTIME_obmc-fan-presence = "phosphor-pid-control"
VIRTUAL-RUNTIME_obmc-sensors-hwmon = "dbus-sensors"

# OpenBMC phosphor-defaults.inc sets this to phosphor-ipmi-fru.
# Remove it to use the phosphor-host-ipmid dbus-sdr FRU write command.
VIRTUAL-RUNTIME_phosphor-ipmi-providers = ""

# runqemu support
QEMUVERSION ?= "10.0%"
EXTRA_IMAGEDEPENDS += "qemu-helper-native"
IMAGE_CLASSES += "qemuboot"
QB_ROOTFS_OPT = "-drive file=@ROOTFS@,if=mtd,format=raw"
QB_DEFAULT_FSTYPE = "static.mtd"
# Disable virtio-pci device
QB_RNG = ""
# Specify netdev to align with default QB_SLIRP_OPT
QB_NETWORK_DEVICE = "-net nic,netdev=net0"
# Boot from drive
QB_DEFAULT_KERNEL = "none"

