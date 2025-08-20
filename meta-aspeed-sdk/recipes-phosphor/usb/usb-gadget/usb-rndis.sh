#!/bin/sh

IP=192.168.20.1
GADGET_BASE=/sys/kernel/config/usb_gadget
CONFIG_FILE="/etc/usb-gadget.conf"
if [ -f "$CONFIG_FILE" ]; then
    . "$CONFIG_FILE"
    echo "USB_VHUB: $USB_VHUB" >&2
else
    USB_VHUB=1e6a0000.usb-vhub
    echo "Config file $CONFIG_FILE not found, using default USB_VHUB: $USB_VHUB" >&2
fi
NAME=netusb

which_dev()
{
    in_use="$(cat $GADGET_BASE/*/UDC)"
    cd /sys/class/udc || exit
    for dev in $USB_VHUB*; do
        case "$in_use" in
            *"$dev"*) ;;
            *) echo "$dev"; return 0;;
        esac
    done
    return 1
}

usb_net_create()
{
    if [ -d $GADGET_BASE/$NAME ]; then
        echo "device $NAME already exists" >&2
        return 1
    fi
    mkdir $GADGET_BASE/$NAME
    cd $GADGET_BASE/$NAME

    echo 0x1d6b > idVendor  # Linux Foundation
    echo 0x0105 > idProduct # FunctionFS Gadget
    mkdir strings/0x409
    local serial="OBMCNET001"
    echo $serial > strings/0x409/serialnumber
    echo OpenBMC > strings/0x409/manufacturer
    echo "OpenBMC Net" > strings/0x409/product

    mkdir configs/c.1
    mkdir functions/rndis.$NAME
    mkdir configs/c.1/strings/0x409

    echo "Config-1" > configs/c.1/strings/0x409/configuration
    echo 120 > configs/c.1/MaxPower
    ln -s functions/rndis.$NAME configs/c.1
    echo $(which_dev) > UDC
}

if test "$1" = stop; then
    ip link set usb0 down
    rm -f $GADGET_BASE/$NAME/configs/c.1/rndis.$NAME
    rmdir $GADGET_BASE/$NAME/configs/c.1/strings/0x409
    rmdir $GADGET_BASE/$NAME/configs/c.1
    rmdir $GADGET_BASE/$NAME/functions/rndis.$NAME
    rmdir $GADGET_BASE/$NAME/strings/0x409
    rmdir $GADGET_BASE/$NAME
else
    usb_net_create
    ip addr add $IP/24 dev usb0
    ip link set usb0 up
fi


