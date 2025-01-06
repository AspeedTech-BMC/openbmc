# The AST2700 DCSCM rofs does not have enough space. 
# Remove the following tool to free up space in the rofs.
RDEPENDS:${PN}-apps:remove = " \
    iozone3 \
    hdparm \
    fio \
    dhrystone \
    "
