# aspeed-zephyr-project bootmcu
SRC_URI_ASPEED_ZEPHYR_PROJECT = "gitsm://gerrit.aspeed.com:29418/aspeed-zephyr-project;protocol=ssh"
ASPEED_ZEPHYR_PROJECT_BRANCH ??= "mcu-runtime"
ASPEED_ZEPHYR_PROJECT_NOBRANCH ??= "0"
SRCREV_app ??= "${AUTOREV}"

SRC_URI += "\
    ${SRC_URI_ASPEED_ZEPHYR_PROJECT};name=app;branch=${ASPEED_ZEPHYR_PROJECT_BRANCH};destsuffix=${S}/aspeed-zephyr-project;nobranch=${ASPEED_ZEPHYR_PROJECT_NOBRANCH} \
"

ZEPHYR_MODULES:append = "\
${S}/aspeed-zephyr-project\;\
"

ZEPHYR_SRC_DIR ??= "${S}/aspeed-zephyr-project/apps/mcu-runtime"
