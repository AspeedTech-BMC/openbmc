# On kernel 5.15 and 6.6, tools/perf/Makefile.config tests BUILD_BPF_SKEL
# with "ifdef" instead of "ifeq ($(BUILD_BPF_SKEL),1)", so the oe-core
# default of passing BUILD_BPF_SKEL=0 to disable the feature does not work:
# "ifdef" is true for any assigned value, including "0", which still trips
# the clang-bpf-co-re check and fails the build when a suitable clang isn't
# available. This was fixed upstream in commit 9925495d96ef ("perf build:
# Default BUILD_BPF_SKEL, warn/disable for missing deps"), which landed for
# v6.7 and was never backported to the 5.15.y/6.6.y stable branches.
#
# GNU Make's "ifdef" only tests whether a variable was ever assigned a
# value, not whether that value is truthy, but a command-line assignment
# of an *empty* value ("VAR=") is treated the same as never having been
# assigned. So pass BUILD_BPF_SKEL= (empty) instead of BUILD_BPF_SKEL=0 to
# actually disable it on the affected kernel versions. Newer kernels
# (6.12+) already do the right thing with BUILD_BPF_SKEL=0, so leave those
# alone.
def perf_bpf_skel_disable_arg(d):
    ver = d.getVar('PREFERRED_VERSION_linux-aspeed') or ''
    if ver.startswith('5.15') or ver.startswith('6.6'):
        return 'BUILD_BPF_SKEL='
    return 'BUILD_BPF_SKEL=0'

PACKAGECONFIG[bpf-skel] = "BUILD_BPF_SKEL=1,${@perf_bpf_skel_disable_arg(d)}"
