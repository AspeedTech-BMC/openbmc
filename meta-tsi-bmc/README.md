# meta-tsi-bmc

OpenBMC Yocto layer for **TSI BMC** support. This is the **Stage 1** skeleton
from the [TSI BMC Master Timeline](../.cursor/plans/tsi_bmc_master_timeline_6b2b8ba4.plan.md):
a registered layer, six machine configurations, a 64 MiB flash layout, and the
`tsi-obmc-phosphor-image` recipe, all building on an **ASPEED AST2700 BSP
stand-in** so the OpenBMC out-of-band (OOB) stack can be built and booted on
QEMU before the real TSI BSP exists (Stage 3/4).

## Status

| Item | State |
|------|-------|
| Layer registers / parses | yes (`bitbake -c parse`) |
| 64 MiB NOR flash layout | `tsi-flash-layout.inc` (`FLASH_SIZE = "65536"`) |
| Six machine confs | yes (see below) |
| `tsi-obmc-phosphor-image` | yes (mirrors reference image for Stage 1 parity) |
| BSP | ASPEED AST2700 SDK stand-in (real TSI BSP arrives Stage 3/4) |

## Layout

```
meta-tsi-bmc/
├── conf/
│   ├── layer.conf
│   ├── machine/
│   │   ├── include/
│   │   │   ├── tsi-bmc-common.inc      # shared base (ASPEED stand-in + OOB features)
│   │   │   └── tsi-flash-layout.inc    # 64 MiB NOR layout (FLASH_SIZE=65536)
│   │   ├── tsi-bmc-qemu-ast2700.conf   # Stage 1 primary QEMU target
│   │   ├── tsi-bmc-tsisim.conf         # Stage 4/5 (TSISIM)
│   │   ├── tsi-bmc-evb.conf            # Stage 6 (EVB silicon)
│   │   ├── tsi-bmc-arm-host.conf       # Stage 7A (ARM host)
│   │   ├── tsi-bmc-amd-x86.conf        # Stage 7B (AMD EPYC)
│   │   └── tsi-bmc-intel-x86.conf      # Stage 7C (Intel x86)
│   └── templates/default/             # `source setup tsi-bmc-qemu-ast2700`
├── recipes-phosphor/images/tsi-obmc-phosphor-image.bb
├── recipes-bsp/u-boot/u-boot-aspeed-sdk_%.bbappend   # BSP hook (stand-in)
└── recipes-kernel/linux/linux-aspeed_%.bbappend      # BSP hook (stand-in)
```

## Machines

| Machine | Target stage | Notes |
|---------|--------------|-------|
| `tsi-bmc-qemu-ast2700` | Stage 1 | QEMU bring-up; AST2700 EVB DTs; boot parity with reference on 64 MiB NOR |
| `tsi-bmc-tsisim` | Stage 4/5 | Full OOB stack on the TSISIM functional simulator using the TSI BSP |
| `tsi-bmc-evb` | Stage 6 | First TSI silicon on the eval board; CMRT/SMC secure boot |
| `tsi-bmc-arm-host` | Stage 7A | ARM host interfaces (in-band IPMI, SOL, MCTP/PLDM over I3C) |
| `tsi-bmc-amd-x86` | Stage 7B | AMD EPYC host; APML (`sbrmi`/`sbtsi`) over I3C in Redfish |
| `tsi-bmc-intel-x86` | Stage 7C | Intel x86 host; eSPI/LPC/KCS + PECI (gated on silicon) |

All six currently share `tsi-bmc-common.inc` and the ASPEED AST2700 BSP
stand-in. Each conf documents what its later stage swaps in (TSI BSP recipes,
device trees, host-interface packages).

## Flash layout (64 MiB NOR)

`tsi-flash-layout.inc` sets `FLASH_SIZE = "65536"`, which activates the
`:flash-65536` override band, then places the partitions (offsets in KiB,
modelled on the proven 64 MiB AST2700 layout in
`meta-aspeed-sdk/meta-ast2700-pfr/conf/machine/ast2700-dcscm.conf`):

| Region | Offset (KiB) | Notes |
|--------|-------------:|-------|
| Caliptra FW | 0 | AST2700 stand-in front matter (Stage 1 only) |
| BootMCU/SPL | 128 | AST2700 stand-in front matter (Stage 1 only) |
| U-Boot | 0 (merged blob) | `FLASH_UBOOT_OFFSET` |
| U-Boot env | 4096 | `FLASH_UBOOT_ENV_OFFSET` |
| Kernel FIT | 4224 | `FLASH_KERNEL_OFFSET` |
| ROFS (squashfs) | 13440 | `FLASH_ROFS_OFFSET` |
| RWFS (jffs2) | 50944 | `FLASH_RWFS_OFFSET` |
| **Total** | **65536** | `FLASH_SIZE = "65536"` |

The Caliptra/BootMCU front matter is AST2700-specific and is expected to shrink
or disappear once the TSI BSP boot chain replaces the stand-in.

## Build

```sh
# From the repo root:
. setup tsi-bmc-qemu-ast2700           # uses this layer's template
bitbake -c parse tsi-obmc-phosphor-image   # Stage 1 task 1 gate
bitbake tsi-obmc-phosphor-image            # full image
```

To add the layer to an existing build instead:

```sh
bitbake-layers add-layer /scratch/smehta/openbmc/meta-tsi-bmc
```

QEMU bring-up follows the Stage 0 procedure
([docs/stage0/run-qemu-ast2700.sh](../docs/stage0/run-qemu-ast2700.sh)),
pointing `IMGDIR` at the `tsi-bmc-qemu-ast2700` deploy directory.

## BSP stand-in and hooks

Stage 1 has no TSI silicon BSP, so the machines reuse the ASPEED AST2700 SDK
(boot ROM → Ibex SPL → Caliptra → TF-A BL31 → OP-TEE → U-Boot → Linux). The
integration hooks live as bbappends on the stand-in recipes:

- `recipes-bsp/u-boot/u-boot-aspeed-sdk_%.bbappend`
- `recipes-kernel/linux/linux-aspeed_%.bbappend`

They establish `FILESEXTRAPATHS` search paths and a `tsi-bmc` machine override
for TSI-specific drop-ins. When the TSI BSP lands (Stage 4+), the U-Boot and
kernel ports move to dedicated `u-boot-tsi` / `linux-tsi` recipes and these
hooks are retargeted there.
