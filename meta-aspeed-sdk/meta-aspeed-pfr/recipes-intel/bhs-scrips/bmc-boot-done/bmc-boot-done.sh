#!/bin/bash

read_id() {
    local FM_BOARD_SKU_ID0="3 22"     #BMC_GPI11
    local FM_BOARD_SKU_ID1="3 24"     #BMC_GPI12
    local FM_BOARD_SKU_ID2="3 26"     #BMC_GPI13
    local FM_BOARD_SKU_ID3="3 28"     #BMC_GPI14
    local FM_BOARD_SKU_ID4="3 30"     #BMC_GPI15
    local FM_BOARD_SKU_ID5="3 32"     #BMC_GPI16
    local result=0
    local value=0
    for pin in "$FM_BOARD_SKU_ID5" "$FM_BOARD_SKU_ID4" "$FM_BOARD_SKU_ID3" "$FM_BOARD_SKU_ID2" "$FM_BOARD_SKU_ID1" "$FM_BOARD_SKU_ID0"; do
      val=$(gpioget $pin)
      value="${value}${val}"
    done
    # Convert binary to hexadecimal
    echo $((2#$value))
}

board_id=$(read_id)

echo "Board ID=$board_id"

GPIO_NAME="BMC_BOOT_DONE"

if gpio=$(gpiofind $GPIO_NAME); then
  echo "$GPIO_NAME asserted"
  gpioset  $gpio=1
else
  echo "$GPIO_NAME not found"
fi

# I don't have Intel board ID definition, so I assume other boards are Intel OKS platform.
INTEL_OKS="0"
case $board_id in
        37)
            name="AvenueCity"
            ;;
        *)  name="Intel OKS"
            INTEL_OKS="1"
            ;;
esac

if [ "$INTEL_OKS" == "1" ]; then
  # GPIOs for Intel OKS bringup
  SMBUS_RDY="3 14"            #BMC_GPI7
  SRC_TO_DEST_FAIL="3 59"     #BMC_GPO29
  BIFURCATION_CFG_DONE="3 57" #BMC_GPO28
  CPU_S5_ENA="3 69"           #BMC_GPO34
  PFR_BMC_ONCTL_N="2 137"     #SREG_GPO4, BMC control SGPO68,
  NODE_ID0="3 27"             #BMC_GPO13
  NODE_ID1="3 29"             #BMC_GPO14

  # Set BootComplete to PFR. 
  # This is a workaround. If no network waiting pfr-amanger send bootcomplete too slow. 
  aspeed-pfr-tool -w 0x60 9
  
  # Wait for SMBUS_RDY to be 1
  timeout=60
  while [ "$(gpioget $SMBUS_RDY)" == "0" ] && [ $timeout -gt 0 ]; do
    echo "SMBUS_RDY is 0, waiting..."
    sleep 1
    timeout=$((timeout - 1))
  done

  if [ "$(gpioget $SMBUS_RDY)" == "1" ]; then
    echo "SMBUS_RDY is 1"
  else
    echo "Timeout reached, SMBUS_RDY is still 0"
  fi

  echo "Set GPIO for Intel OKS bringup"
  gpioset  $SRC_TO_DEST_FAIL=0
  gpioset  $BIFURCATION_CFG_DONE=1
  gpioset  $CPU_S5_ENA=1
  gpioset  $PFR_BMC_ONCTL_N=1

  # Set NODE_ID to 0 for is_legacy.
  gpioset  $NODE_ID0=0
  gpioset  $NODE_ID1=0
fi
