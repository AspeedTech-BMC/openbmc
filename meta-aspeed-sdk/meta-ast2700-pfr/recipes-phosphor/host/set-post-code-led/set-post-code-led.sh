#!/bin/bash

# Define GPIO pins
GPIO_PINS=(BIOS_POST_CODE_LED_7 BIOS_POST_CODE_LED_6 BIOS_POST_CODE_LED_5 BIOS_POST_CODE_LED_4 \
           BIOS_POST_CODE_LED_3 BIOS_POST_CODE_LED_2 BIOS_POST_CODE_LED_1 BIOS_POST_CODE_LED_0 )

# Set GPIO value
# ${1}: BIOS post code
set_gpio() {
    local hex_code=$1

    # Validate hex format
    if ! [[ "$hex_code" =~ ^[0-9a-fA-F]+$ ]]; then
        echo "Invalid postcode (not hex): $hex_code"
        return
    fi

    # Convert the hexadecimal postcode to decimal
    local dec_code=$((16#$hex_code))

    # Convert the decimal number to binary
    binary=""
    while [ "$dec_code" -ne 0 ]; do
        remainder=$((dec_code % 2))
        binary="$remainder$binary"
        dec_code=$((dec_code / 2))
    done

    # Pad the binary number to 8 bits
    while [ ${#binary} -lt 8 ]; do
        binary="0$binary"
    done

    #echo "binary=$binary"
    for ((i=0; i<8; i++)); do
        gpio_pin=$(gpiofind "${GPIO_PINS[$i]}")
        if [ -z "$gpio_pin" ]; then
            echo "set-post-code-led ${GPIO_PINS[$i]} not found"
            exit 1
        fi
        binary_digit=${binary:$i:1}
        gpioset ${gpio_pin}=${binary_digit}
        #echo "gpio_pin=$gpio_pin binary_digit=$binary_digit"
    done
}


# Initial GPIO value.
postcode=$(busctl get-property xyz.openbmc_project.State.Boot.Raw /xyz/openbmc_project/state/boot/raw0 \
                  xyz.openbmc_project.State.Boot.Raw Value | awk '{print $2}')
set_gpio "$postcode"


# Monitor bios post code to set GPIO value
dbus-monitor --system type='signal',interface='org.freedesktop.DBus.Properties',\
member='PropertiesChanged',arg0namespace='xyz.openbmc_project.State.Boot.Raw' | \
while read -r line; do
    grep -q member <<< "$line" && continue

    if grep -q "array of bytes" <<< "$line"; then
        read -r value_line
        postcode=$(echo "$value_line" | tr -d '[][:space:]')

        # Validate hex format
        if ! [[ "$postcode" =~ ^[0-9a-fA-F]{1,2}$ ]]; then
            continue
        fi

        #echo "Detected postcode: $postcode"
        set_gpio "$postcode"
    fi
done
