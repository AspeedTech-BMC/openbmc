MACHINE_FEATURES:append = " ast-ufs"
MACHINEOVERRIDES .= ":ast-ufs"

# This IMAGE_OVERHEAD_FACTOR allows for a percentage overage of the actual
# image size rather than a fixed extra space, this is space needed for
# initial startup and basic operations.
# IMAGE_OVERHEAD_FACTOR is 1.3 by default in bitbake.conf.
# And the IMAGE_ROOTFS_SIZE is IMAGE_ROOTFS * 1.3 by default.
# The rootfs is a readonly filesystem in openbmc for emmc boot.
# Set IMAGE_OVERHEAD_FACTOR 1 to reduce the code size.
IMAGE_OVERHEAD_FACTOR = "1.0"

# If generating an empty image the size of the sparse block should be large
# enough to allocate an ext4 filesystem using 4096 bytes per inode, this is
# about 60K, so add extra rootfs space 4MB
# Defines additional free disk space created in the image in Kbytes
IMAGE_ROOTFS_EXTRA_SPACE = "4096"

# OpenBMC uses wic.xz by default. Add support for wic.gz to flash EMMC/UFS image at U-Boot.
IMAGE_FSTYPES += " wic.gz mmc-ext4-tar"

# Use WIC_SECTOR_SIZE and WIC_BLOCK_SIZE to define the sizes for UFS storage.
WIC_SECTOR_SIZE ?= "4096"
WIC_BLOCK_SIZE ?= "4096"
WICVARS:append = " WIC_SECTOR_SIZE WIC_BLOCK_SIZE"

# Setting block size for mkfs.ext4
EXTRA_IMAGECMD:ext4:append = " -b 4096"
