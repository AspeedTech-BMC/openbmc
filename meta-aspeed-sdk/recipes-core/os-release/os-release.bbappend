FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

python do_compile:prepend() {
    import re
    work_dir = d.getVar("COREBASE")
    sdk_ver = ""
    try:
        res = ""
        try:
            res = bb.process.run("git -C %s symbolic-ref -q --short HEAD"
                                 % work_dir)[0].strip("\n")
        except bb.process.ExecutionError:
            try:
                res = bb.process.run("git -C %s describe --tags --exact-match"
                                     % work_dir)[0].strip("\n")
            except bb.process.ExecutionError:
                pass

        tag_ver = re.search(r"(v\d+\.\d+)", res)
        if tag_ver:
            sdk_ver = tag_ver.group(1)
        else:
            sdk_ver = bb.process.run("git -C %s rev-parse HEAD"
                                     % work_dir)[0].strip("\n")
    except Exception as e:
        print(e)
        bb.warn("Failed to get SDK version")
        pass
    d.setVar("SDK_VERSION", sdk_ver)
}

OS_RELEASE_FIELDS:append = " SDK_VERSION"
BB_DONT_CACHE = "1"

