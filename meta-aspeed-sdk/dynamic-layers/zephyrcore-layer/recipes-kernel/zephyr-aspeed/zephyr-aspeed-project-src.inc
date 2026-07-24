# aspeed-zephyr-project bootmcu
SRC_URI_ASPEED_ZEPHYR_PROJECT = "git://github.com/AspeedTech-BMC/aspeed-zephyr-project;protocol=https"
ASPEED_ZEPHYR_PROJECT_BRANCH ??= "aspeed-master"
ASPEED_ZEPHYR_PROJECT_NOBRANCH ??= "0"

# Tag for v03.07
SRCREV_app = "1e85331603dc82f8a9cfa433f1044567e4ed7b00"

# Submodule
# dtc
ASPEED_ZEPHYR_PROJECT_SUBMODULE_DTC ?= "0"
SRC_URI_DTC = "git://github.com/dgibson/dtc;protocol=https"
SRCREV_dtc = "99031e3a4a6e479466ae795790b44727434ca27d"
SUBMODULE_DTC_PATH = "apps/mcu-runtime/lib/dtc"

# libspdm
ASPEED_ZEPHYR_PROJECT_SUBMODULE_LIBSPDM ?= "0"
SRC_URI_LIBSPDM = "git://github.com/DMTF/libspdm;protocol=https"
SRCREV_libspdm = "5ebe5e3946b9439928fa3a7548268c29cccc1b16"
SUBMODULE_LIBSPDM_PATH = "lib/dmtf/libspdm"

SRC_URI += "\
    ${SRC_URI_ASPEED_ZEPHYR_PROJECT};name=app;branch=${ASPEED_ZEPHYR_PROJECT_BRANCH};destsuffix=${S}/aspeed-zephyr-project;nobranch=${ASPEED_ZEPHYR_PROJECT_NOBRANCH} \
    ${@oe.utils.conditional('ASPEED_ZEPHYR_PROJECT_SUBMODULE_DTC', '1', \
        '${SRC_URI_DTC};name=dtc;destsuffix=${S}/aspeed-zephyr-project/${SUBMODULE_DTC_PATH};nobranch=1', \
        '', d)} \
    ${@oe.utils.conditional('ASPEED_ZEPHYR_PROJECT_SUBMODULE_LIBSPDM', '1', \
        '${SRC_URI_LIBSPDM};name=libspdm;destsuffix=${S}/aspeed-zephyr-project/${SUBMODULE_LIBSPDM_PATH};nobranch=1', \
        '', d)} \
"

ZEPHYR_MODULES:append = "\
${S}/aspeed-zephyr-project\;\
"

python do_check_submodule_version() {
    import os
    import subprocess

    submodules = [
        (
            "dtc",
            d.getVar("SUBMODULE_DTC_PATH"),
            d.getVar("SRCREV_dtc")
        ),
        (
            "libspdm",
            d.getVar("SUBMODULE_LIBSPDM_PATH"),
            d.getVar("SRCREV_libspdm")
        )
    ]

    srcdir = os.path.join(d.getVar("S"), "aspeed-zephyr-project")

    for name, path, expected in submodules:

        try:
            result = subprocess.run(
                [ "git", "ls-tree", "HEAD", path],
                cwd=srcdir,
                check=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )

            line = result.stdout.strip()

            if not line:
                bb.error("Submodule not found %s" % path)
                continue

            actual = line.split()[2]

            if actual != expected:
                bb.warn("Submodule version mismatch [%s]: expected: %s, actual: %s" %
                        (name, expected, actual))
            else:
                print("Submodule version match [%s]: %s" % (name, actual))

        except subprocess.CalledProcessError as e:
            bb.warn("Failed to check submodule [%s]: %s" % (name, e.stderr))
}

addtask check_submodule_version before do_configure after do_unpack

