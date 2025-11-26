DESCRIPTION = "Linux kernel for Aspeed"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"

PROVIDES += "virtual/kernel"

KCONFIG_MODE = "--alldefconfig"
KERNEL_VERSION_SANITY_SKIP = "1"

KSRC = "git://github.com/AspeedTech-BMC/linux.git;protocol=https;branch=${KBRANCH}"
SRC_URI = "${KSRC}"

LINUX_VERSION_EXTENSION ?= "-${SRCREV}"

PV = "${LINUX_VERSION}+git"
inherit kernel
require recipes-kernel/linux/linux-yocto.inc

ERROR_QA:remove = "buildpaths"
WARN_QA:append = "buildpaths"

LIC_FILES_CHKSUM ?= "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Do not install the kernel image into rootfs at /boot directory
RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base = ""

python do_set_local_version() {
    s = d.getVar("S")
    b = d.getVar("B")
    conf_local_ver = ""

    with open("%s/.config" % b, "r+") as f:
        lines = f.readlines()
        f.seek(0)
        for line in lines:
            if line.find("CONFIG_LOCALVERSION") == -1:
                f.write(line)
        f.truncate()

    try:
        res=bb.process.run("git -C %s describe --tags --exact-match" %  s)[0].strip("\n")

        if res.find("devtool") != -1:
            version_ext = bb.process.run("git -C %s rev-parse --verify --short HEAD" % s)[0].strip("\n")
            conf_local_ver = ('CONFIG_LOCALVERSION=\"-%s-%s\"' % res, version_ext)
        else:
            conf_local_ver = ('CONFIG_LOCALVERSION=\"-%s\"' % res)
    except Exception as e:
        version=bb.process.run("git -C %s rev-parse --verify --short HEAD" % s)[0].strip("\n")
        conf_local_ver = ('CONFIG_LOCALVERSION=\"-dirty-%s\"' % version)
    with open("%s/.config" % b, "a") as f:
        f.write(conf_local_ver)
}

addtask set_local_version before do_configure after do_kernel_configme

