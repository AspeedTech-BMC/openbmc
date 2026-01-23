#!/bin/bash
# This script was modified from AMD OpenBMC platform-init.sh
# https://github.com/AMDESE/OpenBMC/blob/SP7/meta-amd/meta-sp7/recipes-amd/platform-init/files/platform-init.sh
#

# Function to set GPIO pin using gpiofind
set_gpio_by_name() {
    local gpio_name="$1"
    local value="$2"

    if [ -z "$gpio_name" ] || [ -z "$value" ]; then
        echo "Usage: set_gpio_by_name <gpio_name> <value>"
        return 1
    fi

    if gpio=$(gpiofind "$gpio_name"); then
        echo "Setting $gpio_name($gpio) to $value"
        gpioset $gpio=$value
    else
        echo "GPIO $gpio_name not found"
        return 1
    fi
}

echo "platform-init: start"

set_gpio_by_name "P0_MGMT_ASSERT_PROCHOT_L" 0
set_gpio_by_name "P0_MGMT_ASSERT_CLR_CMOS" 0
set_gpio_by_name "P0_ASSERT_RSMRST" 0
set_gpio_by_name "P0_MGMT_ASSERT_THERMTRIP_L" 0
set_gpio_by_name "P0_MGMT_ASSERT_WARM_RST_BTN_L" 1

set_gpio_by_name "HPM_STBY_EN" 1
set_gpio_by_name "P0_MGMT_UPDATE_FLASH_0" 0
set_gpio_by_name "P0_MGMT_UPDATE_FLASH_1" 0
set_gpio_by_name "P0_MGMT_UPDATE_FLASH_2" 0

# From AMD U-Boot evb_ast2700.c configure_edaf_spi() function
# Reconfigure pin from SCM_GPO to GPIO mode.
devmem 0x14c02404 32 0x55000050
gpioset 1 10=0
gpioset 1 11=0
