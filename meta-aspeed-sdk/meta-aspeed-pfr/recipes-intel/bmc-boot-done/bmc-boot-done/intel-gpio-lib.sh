#!/bin/bash
# intel-gpio-lib.sh - Shared GPIO utility functions for PFR scripts
#
# Source this file to get access to:
#   find_gpiochip <gpio_type>   - returns gpiochip number for given controller
#   read_id                     - returns decimal board SKU ID from LTPI0 GPIO pins

# Find the gpiochip number for a specific GPIO controller.
# Usage: find_gpiochip <gpio_type>
# Returns: gpiochip number (integer), or error on stderr and return 1
find_gpiochip() {
    local gpio_type="$1"
    local chip_info
    chip_info=$(gpiodetect | grep "$gpio_type")
    if [ -n "$chip_info" ]; then
        echo "${chip_info//gpiochip/}" | cut -d' ' -f1
    else
        echo "Error: $gpio_type not found" >&2
        return 1
    fi
}

# Read the board SKU ID from LTPI0 GPIO pins (FM_BOARD_SKU_ID[5:0]).
# Returns: decimal value of the 6-bit board ID, or returns 1 on failure
read_id() {
    local ltpi0_gpio
    if ! ltpi0_gpio=$(find_gpiochip "ltpi0-gpio"); then
        echo "Failed to find ltpi0-gpio chip" >&2
        return 1
    fi

    local FM_BOARD_SKU_ID0="${ltpi0_gpio} 22"     #BMC_GPI11
    local FM_BOARD_SKU_ID1="${ltpi0_gpio} 24"     #BMC_GPI12
    local FM_BOARD_SKU_ID2="${ltpi0_gpio} 26"     #BMC_GPI13
    local FM_BOARD_SKU_ID3="${ltpi0_gpio} 28"     #BMC_GPI14
    local FM_BOARD_SKU_ID4="${ltpi0_gpio} 30"     #BMC_GPI15
    local FM_BOARD_SKU_ID5="${ltpi0_gpio} 32"     #BMC_GPI16

    local value=0
    for pin in "$FM_BOARD_SKU_ID5" "$FM_BOARD_SKU_ID4" "$FM_BOARD_SKU_ID3" \
               "$FM_BOARD_SKU_ID2" "$FM_BOARD_SKU_ID1" "$FM_BOARD_SKU_ID0"; do
        # shellcheck disable=SC2086
        local val
        val=$(gpioget $pin)
        value="${value}${val}"
    done

    # Convert binary string to decimal
    echo $((2#$value))
}
