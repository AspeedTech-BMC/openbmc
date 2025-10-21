SUMMARY = "ASPEED FMC images provide the necessary image for AST2700 bring-up"
HOMEPAGE = "https://github.com/AspeedTech-BMC/fmc_imgtool"

require ../../recipes-aspeed/python/fmc-imgtool.inc

inherit deploy

do_deploy () {
  install -d ${DEPLOYDIR}
  install -d ${DEPLOYDIR}/fmc-images
  install -d ${DEPLOYDIR}/keys

  install -m 644 ${S}/prebuilt/* ${DEPLOYDIR}/fmc-images/.
  install -m 644 ${S}/keys/* ${DEPLOYDIR}/keys/.

  # Create symbolic links from fmc-images/* to DEPLOYDIR/
  for f in ${DEPLOYDIR}/fmc-images/*; do
    ln -sf fmc-images/$(basename "$f") ${DEPLOYDIR}/
  done
}

addtask deploy before do_build after do_compile

python do_cleanall:append() {
    import os, shutil, glob

    deploydir = d.getVar('DEPLOYDIR', True)
    if not deploydir:
        return

    # Remove directories
    for sub in ("fmc-images", "keys"):
        p = os.path.join(deploydir, sub)
        if os.path.isdir(p):
            bb.note(f"Removing {p}")
            shutil.rmtree(p, ignore_errors=True)

    # Remove symbolic links in DEPLOYDIR that point to fmc-images/*
    for path in glob.glob(os.path.join(deploydir, "*")):
        if os.path.islink(path):
            try:
                target = os.readlink(path)
            except OSError:
                continue
            if target.startswith("fmc-images/"):
                bb.note(f"Removing symlink {path} -> {target}")
                os.unlink(path)
}
