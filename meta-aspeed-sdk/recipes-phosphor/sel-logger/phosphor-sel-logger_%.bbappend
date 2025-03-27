PACKAGECONFIG:append = " log-threshold"

# ToDo below commit cause build fail.
# https://github.com/openbmc/phosphor-sel-logger/commit/9916d41882ff7660578edeebad2b6559d7941229
# ../git/include/sel_logger.hpp:85:29: error: there are no arguments to 'getNewRecordId' 

SRCREV = "02124a1edaa68df4a2b368d71a6f67e0275d5ba9"
