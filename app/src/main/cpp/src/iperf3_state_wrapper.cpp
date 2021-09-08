#include <android/log.h>

#include "iperf3_state_wrapper.h"
#include "iperf_api.h"

#include <vector>

static IperfStateWrapper iperf_wrapper = IperfStateWrapper();

struct iperf_test_state * new_client_wrapper() {
    return iperf_wrapper.create_new_test();
}

int run_wrapper()
{
    return iperf_wrapper.run_test();
}


int stop_wrapper() {
    return iperf_wrapper.stop_test();
}

void delete_client_wrapper() {
    iperf_wrapper.finalize_test();
}

IperfStateWrapper::IperfStateWrapper() noexcept:
        _test_state(),
        _run_mutex(),
        _stop_signal_mutex() {}

struct iperf_test_state * IperfStateWrapper::create_new_test() {
    _run_mutex.lock();
    std::scoped_lock lock(_stop_signal_mutex);
    _test_state.iperf_test = iperf_new_test();
    if (!_test_state.iperf_test) {
        _run_mutex.unlock();
        return nullptr;
    }

    // Intentionally do not unlock the run mutex, will be released as part of the close function.
    return &_test_state;
}

int IperfStateWrapper::run_test() {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering run_wrapper");
    std::scoped_lock lock(_run_mutex);

    /* Ignore SIGPIPE to simplify error handling */
    signal(SIGPIPE, SIG_IGN);

    struct iperf_test * test = _test_state.iperf_test;
    switch (test->role) {
        case 'c':
            __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Running client in run_wrapper");
            if (iperf_create_pidfile(test) < 0) {
                __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Failed to create pidfile");
                i_errno = IEPIDFILE;
                iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
            }
            if (iperf_run_client(test) < 0) {
                __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Failed to run client: %s", iperf_strerror(i_errno));
                iperf_delete_pidfile(test);
                signal(SIGPIPE, SIG_DFL);
                return -1;
            }
            iperf_delete_pidfile(test);
            break;
        case 's':
        default:
            __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Instructed to start an unsupported role %c!", test->role);
            signal(SIGPIPE, SIG_DFL);
            return -1;
    }

    signal(SIGPIPE, SIG_DFL);
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting run_wrapper");
    return 0;
}

int IperfStateWrapper::stop_test() {
    std::scoped_lock lock(_stop_signal_mutex);
    __android_log_print(ANDROID_LOG_INFO, __FILE_NAME__, "Entering stop_wrapper");
    if (_test_state.iperf_test) {
        // TODO(matt9j) Done really needs to be atomic : /
        _test_state.iperf_test->done = 1;
    }
    __android_log_print(ANDROID_LOG_INFO, __FILE_NAME__, "Exiting stop_wrapper");
    return 0;
}

void IperfStateWrapper::finalize_test() {
    std::scoped_lock lock(_run_mutex, _stop_signal_mutex);
    iperf_free_test(_test_state.iperf_test);
    _run_mutex.unlock();
}
