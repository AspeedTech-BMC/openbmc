#!/bin/bash

# Function to find the gpiochip number for specific gpio controller
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

read_id() {
    local FM_BOARD_SKU_ID0="${LTPI0_GPIO} 22"     #BMC_GPI11
    local FM_BOARD_SKU_ID1="${LTPI0_GPIO} 24"     #BMC_GPI12
    local FM_BOARD_SKU_ID2="${LTPI0_GPIO} 26"     #BMC_GPI13
    local FM_BOARD_SKU_ID3="${LTPI0_GPIO} 28"     #BMC_GPI14
    local FM_BOARD_SKU_ID4="${LTPI0_GPIO} 30"     #BMC_GPI15
    local FM_BOARD_SKU_ID5="${LTPI0_GPIO} 32"     #BMC_GPI16
    local value=0
    for pin in "$FM_BOARD_SKU_ID5" "$FM_BOARD_SKU_ID4" "$FM_BOARD_SKU_ID3" "$FM_BOARD_SKU_ID2" "$FM_BOARD_SKU_ID1" "$FM_BOARD_SKU_ID0"; do
      # shellcheck disable=SC2086
      val=$(gpioget $pin)
      value="${value}${val}"
    done
    # Convert binary to hexadecimal
    echo $((2#$value))
}

GPIO_NAME="BMC_BOOT_DONE"

if gpio=$(gpiofind $GPIO_NAME); then
  echo "$GPIO_NAME asserted"
  # shellcheck disable=SC2086
  gpioset $gpio=1
else
  echo "$GPIO_NAME not found"
fi

SOC_FAMILY=$(cat /sys/bus/soc/devices/soc0/family)

if [ "${SOC_FAMILY}" == "AST2600" ]; then
    echo "${SOC_FAMILY}, skipping GPIO configuration."
    exit 0
fi

# Get the ltpi0-gpio chip number
if ! LTPI0_GPIO=$(find_gpiochip "ltpi0-gpio"); then
    echo "Failed to find ltpi0-gpio chip"
    exit 1
fi

# Get the sgpio chip number
if ! SGPIO_CHIP=$(find_gpiochip "sgpios"); then
    echo "Failed to find sgpio chip"
    exit 1
fi

echo "Found ltpi0-gpio at gpiochip${LTPI0_GPIO}"
echo "Found sgpios at gpiochip${SGPIO_CHIP}"

board_id=$(read_id)

echo "Board ID=$board_id"

# I don't have Intel board ID definition, so I assume other boards are Intel OKS platform.
INTEL_OKS="0"
case $board_id in
        37)
            # AvenueCity platform
            ;;
        *)
            # Intel OKS platform
            INTEL_OKS="1"
            ;;
esac

if [ "$INTEL_OKS" == "1" ]; then
  # GPIOs for Intel OKS bringup
  SMBUS_RDY="${LTPI0_GPIO} 14"            #BMC_GPI7
  SRC_TO_DEST_FAIL="${LTPI0_GPIO} 59"     #BMC_GPO29
  BIFURCATION_CFG_DONE="${LTPI0_GPIO} 57" #BMC_GPO28
  CPU_S5_ENA="${LTPI0_GPIO} 69"           #BMC_GPO34
  PFR_BMC_ONCTL_N="${SGPIO_CHIP} 137"     #SREG_GPO4, BMC control SGPO68,
  NODE_ID0="${LTPI0_GPIO} 27"             #BMC_GPO13
  NODE_ID1="${LTPI0_GPIO} 29"             #BMC_GPO14

  # Set BootComplete to PFR.
  # This is a workaround. If no network waiting pfr-amanger send bootcomplete too slow. 
  aspeed-pfr-tool -w 0x60 9

  # Wait for SMBUS_RDY to be 1
  timeout=60
  # shellcheck disable=SC2086
  while [ "$(gpioget $SMBUS_RDY)" == "0" ] && [ $timeout -gt 0 ]; do
    echo "SMBUS_RDY is 0, waiting..."
    sleep 1
    timeout=$((timeout - 1))
  done

  # shellcheck disable=SC2086
  if [ "$(gpioget $SMBUS_RDY)" == "1" ]; then
    echo "SMBUS_RDY is 1"
  else
    echo "Timeout reached, SMBUS_RDY is still 0"
  fi

  echo "Set GPIO for Intel OKS bringup"
  # shellcheck disable=SC2086
  gpioset $SRC_TO_DEST_FAIL=0
  gpioset $BIFURCATION_CFG_DONE=1
  gpioset $CPU_S5_ENA=1
  gpioset $PFR_BMC_ONCTL_N=1

  # Set NODE_ID to 0 for is_legacy.
  # shellcheck disable=SC2086
  gpioset $NODE_ID0=0
  gpioset $NODE_ID1=0
fi
