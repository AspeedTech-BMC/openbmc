project('pfr-mctp-i3c', 'c', version: '1.0.0')
thread_dep = dependency('threads')

executable('pfr-mctpd', ['main.c'], install: true, dependencies: thread_dep)
install_data(sources : 'mctp-i3c-starter.sh', install_dir: '/usr/bin', install_mode: 'rwxr-xr-x')