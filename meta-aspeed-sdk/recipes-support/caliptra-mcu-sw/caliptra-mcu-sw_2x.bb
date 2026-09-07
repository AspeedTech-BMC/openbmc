SUMMARY = "Caliptra MCU firmware and software"
HOMEPAGE = "https://github.com/chipsalliance/caliptra-mcu-sw"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

BRANCH = "aspeed-dev-2.1-rt"
SRC_URI = "gitsm://gerrit.aspeed.com:29418/caliptra-mcu-sw;protocol=ssh;branch=${BRANCH}; \
           https://sh.rustup.rs;name=rustup-init;subdir=${UNPACKDIR};downloadfilename=rustup-init.sh;unpack=0 \
          "
SRC_URI[rustup-init.sha256sum] = "6c30b75a75b28a96fd913a037c8581b580080b6ee9b8169a3c0feb1af7fe8caf"

SRCREV = "${AUTOREV}"

B = "${UNPACKDIR}/build"

inherit cargo deploy

PACKAGE_ARCH = "${MACHINE_ARCH}"

SSMCU_RUST_TARGET ?= "riscv32imc-unknown-none-elf"
SSMCU_RUNTIME_PATH ?= "${S}/target/${SSMCU_RUST_TARGET}/release/runtime-ast2705.bin"

# Using cargo to download packages
CARGO_DISABLE_BITBAKE_VENDORING = "1"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

# Prevent fallback to cargo_do_compile
do_compile() {
    :
}

# Build xtask only for native
do_compile:class-native() {
    cd ${S}
    cargo build -p caliptra-mcu-xtask --release
}

# Build the AST2705 SSMCU runtime only for AST27x5 targets.
do_compile:append:ast27x5:class-target() {
    export CARGO_HOME=${B}/cargo_home
    export RUSTUP_HOME=${B}/rustup_home
    sh ${UNPACKDIR}/rustup-init.sh --no-modify-path -y
    . $CARGO_HOME/env

    export OBJCOPY="${STAGING_BINDIR_NATIVE}/llvm-objcopy"

    # Use the native compiler for x86_64 build dependencies instead of the
    # AArch64 TARGET_* compiler from BitBake. Strip trailing space from BUILD_CC.
    host_cc="$(printf '%s' "${BUILD_CC}" | sed 's/[[:space:]]*$//')"
    unset TARGET_CC TARGET_CXX TARGET_CFLAGS TARGET_CXXFLAGS TARGET_AR
    export CC="${host_cc}"
    export CC_x86_64_unknown_linux_gnu="${host_cc}"
    export CFLAGS_x86_64_unknown_linux_gnu="${BUILD_CFLAGS}"
    export CARGO_TARGET_X86_64_UNKNOWN_LINUX_GNU_LINKER="${host_cc}"

    cd ${S}
    cargo xtask runtime-build --platform ast2705 --profile release
}

# Install xtask only for native
do_install() {
    :
}

do_install:class-native() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/target/release/caliptra-mcu-xtask ${D}${bindir}/xtask-2x
}

do_deploy() {
    :
}

# Deploy the AST2705 SSMCU runtime only for AST27x5 targets.
do_deploy:ast27x5:class-target() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${SSMCU_RUNTIME_PATH} ${DEPLOYDIR}/${SSMCU_RUNTIME_BINARY}
}

addtask deploy before do_build after do_compile

BBCLASSEXTEND = "native nativesdk"
