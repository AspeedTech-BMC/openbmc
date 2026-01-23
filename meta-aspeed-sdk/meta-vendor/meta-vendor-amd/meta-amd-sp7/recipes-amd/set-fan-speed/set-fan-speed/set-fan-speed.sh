#!/bin/bash
# This script was modified from AMD OpenBMC set-fan-speed.sh
# https://github.com/AMDESE/OpenBMC/blob/SP7/meta-amd/meta-sp7/recipes-amd/set-fan-speed/files/set-fan-speed.sh
#
set -e

speed_val=$1

init_nct7363_fan_controller()
{
    echo "Initializing fans for Kenya"
    FAN_SET_REG=(
            # PWM initilization Regs, for 3-channel PWM
            "0x2A 0x00"        # Disable WDT for no-fan testing
            "0x38 0x01"        # Enable PWM0
            "0x39 0x81"        # Enable PWM8 and PWM15
            "0x41 0x7E"        # Enable FANIN1-6
            "0x42 0xF6"        # Enable FANIN9-10 & FANIN12-15
            "0x20 0x29"        # Set FANIN10 FANIN9 PWM0
            "0x21 0xAA"        # Set FANIN15 FANIN4 FANIN13 FANIN12
            "0x22 0xA9"        # Set FANIN3 FANIN2 FANIN1 PWM8
            "0x23 0x6A"        # Set PWM15 FANIN6 FANIN5 FANIN4

            # Fan speed control Regs, for 1-6 fans
            "0x90 $speed_val"  # Set PWM0 FSCPxDUTY
            "0xA0 $speed_val"  # Set PWM8 FSCPxDUTY
            "0xAE $speed_val"  # Set PWM15 FSCPxDUTY
    )
    return 0
}

# Enable I2C switch bus 6
i2cset -f -y 0 0x70 0x40

# prepare nct7363 controller registers
init_nct7363_fan_controller || retval=$?
if [[ "$retval" -ne 0 ]]; then
    echo "Error: init_nct7363_fan_controller failed."
    return 1
fi

for reg_val in "${FAN_SET_REG[@]}"; do
    register="${reg_val%% *}"
    value="${reg_val##* }"
    i2cset -f -y 0 0x20 "$register" "$value" || retval=$?

    if [[ "$retval" -ne 0 ]]; then
        echo "Error: set_nct7363_fan_controller failed" \
                "bus:$bus dev:$NCT7363_DEV reg:$register val:$value"
        break
    fi
done

# Monitor PWM0 FSCPxDUTY register every 5 seconds
while true; do
    pwm_value=$(i2cget -f -y 0 0x20 0x90)
    #echo "$(date): PWM0 FSCPxDUTY (0x90) = $pwm_value"
    sleep 5
done
