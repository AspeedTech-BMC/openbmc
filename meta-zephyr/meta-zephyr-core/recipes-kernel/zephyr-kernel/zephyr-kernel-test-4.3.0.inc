# Exclude tests that fail to configure
ZEPHYRTESTS:remove = "fifo \
    fpu_sharing \
    lifo \
    mbox \
    mem_heap \
    mem_pool \
    mem_protect \
    mem_slab \
    msgq \
    mutex \
    pipe \
    profiling \
    sched \
    semaphore \
    stack \
    threads \
    tickless \
    timer \
    workq"

# Exclude tests that are not currently compiling
ZEPHYRTESTS:remove = "ipi_cascade ipi_optimize ipi_work mp smp smp_abort smp_boot_delay smp_suspend  spinlock"
ZEPHYRTESTS:remove:96b-avenger96 = "cache common device poll queue sleep"
ZEPHYRTESTS:remove:stm32mp157c-dk2 = "cache common device poll queue sleep"

# List of all available kernel tests
ZEPHYRTESTS = " \
    cache \
    common \
    context \
    device \
    early_sleep \
    fifo \
    fpu_sharing \
    ipi_cascade \
    ipi_optimize \
    ipi_work \
    lifo \
    mbox \
    mem_heap \
    mem_protect \
    mem_slab \
    mp \
    msgq \
    mutex \
    obj_tracking \
    pending \
    pipe \
    poll \
    profiling \
    queue \
    sched \
    semaphore \
    sleep \
    smp \
    smp_abort \
    smp_boot_delay \
    smp_suspend \
    spinlock \
    stack \
    threads \
    tickless \
    timer \
    workq \
    "
