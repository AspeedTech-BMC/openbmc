FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SYSTEMD_OVERRIDE:${PN} = "hotjoin.conf:pfr-mctp-i3c.service.d/hotjoin.conf"
SYSTEMD_OVERRIDE:${PN}:ast2700-a0 = "hotjoin_a0.conf:pfr-mctp-i3c.service.d/hotjoin_a0.conf"

# Add nostamp to avoid build failure when the machine changes from ast2700-a0 to a1.
do_collect_spdx_deps[nostamp] = "1"
do_configure[nostamp] = "1"
do_compile[nostamp] = "1"
do_install[nostamp] = "1"
