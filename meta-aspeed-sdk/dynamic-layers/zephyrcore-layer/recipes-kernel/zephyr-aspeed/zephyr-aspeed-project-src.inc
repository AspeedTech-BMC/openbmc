# aspeed-zephyr-project bootmcu
SRC_URI_ASPEED_ZEPHYR_PROJECT = "gitsm://gerrit.aspeed.com:29418/aspeed-zephyr-project;protocol=ssh"
ASPEED_ZEPHYR_PROJECT_BRANCH ??= "mcu-runtime"
SRCREV_bootmcu ??= "${AUTOREV}"

SRC_URI += "\
    ${SRC_URI_ASPEED_ZEPHYR_PROJECT};name=bootmcu;branch=${ASPEED_ZEPHYR_PROJECT_BRANCH};destsuffix=${S}/aspeed-zephyr-project \
"

ZEPHYR_MODULES:append = "\
${S}/aspeed-zephyr-project\;\
"

ZEPHYR_SRC_DIR ??= "${S}/aspeed-zephyr-project/apps/mcu-runtime"
