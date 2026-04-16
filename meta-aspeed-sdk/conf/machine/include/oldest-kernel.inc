# glibc
# --enable-kernel=version
# This option is currently only useful on GNU/Linux systems.
# The version parameter should have the form X.Y.Z and describes the smallest
# version of the Linux kernel the generated library is expected to support.
# The higher the version number is, the less compatibility code is added,
# and the faster the code gets.

# Yocto requires the minimum supported kernel version to be set to 5.15
# in bitbake.conf. The ASPEED SDK also supports kernel 5.15 as the oldest
# supported kernel version.
#
# Previously, the ASPEED SDK supported kernel 5.4, while Yocto defaulted to
# kernel 5.15. If OLDEST_KERNEL was not explicitly set to 5.4, this version
# mismatch could lead to unexpected issues, such as glibc incompatibilities
# and a kernel panic caused by systemd failing to start.
#
# The ASPEED SDK now supports kernel 5.15 as the oldest version, so this file
# is no longer strictly required. However, this setting is intentionally kept
# to avoid accidental changes and to document the potential issues caused by
# mismatches between Yocto defaults and ASPEED SDK supported kernel versions.
OLDEST_KERNEL = "5.15"
