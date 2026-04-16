#!/bin/sh
# Monitor xyz.openbmc_project.State.Host CurrentHostState via D-Bus.
# On power-off: bring down mctpi3c4 and stop pfr-mctp-i3c.service.
# On power-on:  rescan I3C bus and start pfr-mctp-i3c.service.

HOST_SERVICE="xyz.openbmc_project.State.Host"
HOST_PATH="/xyz/openbmc_project/state/host0"
HOST_IFACE="xyz.openbmc_project.State.Host"
HOST_PROP="CurrentHostState"

get_host_state() {
    busctl get-property "$HOST_SERVICE" "$HOST_PATH" "$HOST_IFACE" "$HOST_PROP" 2>/dev/null | \
        awk -F'"' '{print $2}'
}

on_host_poweroff() {
    echo "HostState -> Off: bringing down mctpi3c4 and stopping pfr-mctp-i3c"
    ip link set mctpi3c4 down 2>/dev/null || true
    systemctl stop pfr-mctp-i3c.service
}

on_host_poweron() {
    echo "HostState -> Running: rescanning I3C and starting pfr-mctp-i3c"
    echo 1 > /sys/bus/i3c/devices/14c25000.i3c5/rescan
    systemctl start pfr-mctp-i3c.service
}

prev_state=$(get_host_state)
echo "Initial HostState: $prev_state"

while true; do
    sleep 2
    state=$(get_host_state)
    [ -z "$state" ] && continue
    if [ "$state" != "$prev_state" ]; then
        echo "HostState changed: $prev_state -> $state"
        case "$state" in
            *HostState.Off)
                on_host_poweroff ;;
            *HostState.Running)
                on_host_poweron ;;
        esac
        prev_state="$state"
    fi
done
