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
