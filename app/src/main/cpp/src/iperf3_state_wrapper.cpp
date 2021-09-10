#include <android/log.h>

#include "iperf3_state_wrapper.h"
#include "iperf3_java_callback.h"
#include "iperf_api.h"

#include <mutex>

std::mutex singleton_mutex;
IperfStateWrapper* global_state_wrapper;
bool global_stop_requested = false;

extern "C" {

void
send_interval_report(float start, float end, char sent_bytes[], char bandwidth[]) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Enter interval report global function");
    {
        std::scoped_lock lock(singleton_mutex);

        if (global_state_wrapper) {
            __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__,
                                "Interval report acquired lock");
            global_state_wrapper->send_interval_report(start, end, sent_bytes, bandwidth);

            if (global_stop_requested) {
                // There is a race condition where the iperf client will reset the "done" flag while
                // beginning communication with the server in the run_iperf_client function :/
                //
                // This check will safely set the done flag again if a stop has been requested and
                // the global wrapper is present.
                global_state_wrapper->stop_test();
            }
        }
    }
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exit interval report global function");
}

void
send_summary_report(float start, float end, char sent_bytes[], char bandwidth[]) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Enter summary report global function");
    {
        std::scoped_lock lock(singleton_mutex);

        if (global_state_wrapper) {
            __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__,
                                "Enter summary report global function locked section");
            global_state_wrapper->send_summary_report(start, end, sent_bytes, bandwidth);

            if (global_stop_requested) {
                // There is a race condition where the iperf client will reset the "done" flag while
                // beginning communication with the server in the run_iperf_client function :/
                //
                // This check will safely set the done flag again if a stop has been requested and
                // the global wrapper is present.
                global_state_wrapper->stop_test();
            }
        }
    }
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exit summary report global function");
}

int
stop_wrapper() {
    std::scoped_lock lock(singleton_mutex);

    global_stop_requested = true;

    if (global_state_wrapper) {
        global_state_wrapper->stop_test();
        return 0;
    }
    __android_log_print(ANDROID_LOG_WARN, __FILE_NAME__, "Stop called with no global allocated");
    return 1;
}

} // extern "c"

IperfStateWrapper::IperfStateWrapper() noexcept:
    _test_state()
{
    _test_state.iperf_test = iperf_new_test();
    if (!_test_state.iperf_test) {
        __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Failed to allocate a new iperf test");
        __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Intentionally crashing with nonzero exit");
        exit(1);
    }
    iperf_defaults(_test_state.iperf_test);    /* sets defaults */

    {
        std::scoped_lock lock(singleton_mutex);
        if (global_state_wrapper) {
            // There is already a global pointer!!!
            __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Inconsistent global state pointer");
            __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Intentionally crashing with nonzero exit");
            exit(1);
        }

        global_state_wrapper = this;

        if (global_stop_requested) {
            // Handle the edge case that the stop_wrapper was called before the state wrapper was
            // initialized.
            stop_test();
        }
    }
}

IperfStateWrapper::~IperfStateWrapper() noexcept {
    {
        std::scoped_lock lock(singleton_mutex);
        global_state_wrapper = nullptr;
        global_stop_requested = false;
    }

    if (_test_state.iperf_test) {
        iperf_free_test(_test_state.iperf_test);
        _test_state.iperf_test = nullptr;
    }
}

bool
IperfStateWrapper::register_callbacks(JNIEnv *javaEnv, jobject iperfConfig, jobject callback, jstring cacheDirTemplate) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering register_callbacks");
    parse_java_config(javaEnv, &_test_state, iperfConfig, cacheDirTemplate);
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "parsed java config");
    if (construct_java_callback(javaEnv, &_test_state, callback) < 0) {
        return false;
    }

    iperf_set_external_interval_report_callback(_test_state.iperf_test, ::send_interval_report);
    iperf_set_external_summary_report_callback(_test_state.iperf_test, ::send_summary_report);

    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting register_callbacks");
    return true;
}

int
IperfStateWrapper::run_test() {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering run_test");

    /* Ignore SIGPIPE to simplify error handling */
    signal(SIGPIPE, SIG_IGN);

    struct iperf_test * test = _test_state.iperf_test;
    switch (test->role) {
        case 'c':
            if (iperf_create_pidfile(test) < 0) {
                __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Failed to create pidfile");
                i_errno = IEPIDFILE;
                signal(SIGPIPE, SIG_DFL);
                return -1;
            }
            if (iperf_run_client(test) < 0) {
                __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__, "Failed to run client: %s", iperf_strerror(i_errno));

                // jni callback
                char *err_str = iperf_strerror(i_errno);
                _test_state.jniCallback.on_error(&_test_state, err_str);

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
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting run_test");
    return 0;
}

int
IperfStateWrapper::stop_test() {
    __android_log_print(ANDROID_LOG_INFO, __FILE_NAME__, "Entering stop_test");
    if (_test_state.iperf_test) {
        // TODO(matt9j) Done really needs to be atomic since it is set and read from multiple contexts : /
        _test_state.iperf_test->done = 1;
    }
    __android_log_print(ANDROID_LOG_INFO, __FILE_NAME__, "Exiting stop_test");
    return 0;
}

void
IperfStateWrapper::send_interval_report(float start, float end, char sent_bytes[], char bandwidth[]) {
    _test_state.jniCallback.on_interval(&_test_state, start, end, sent_bytes, bandwidth);
}

void
IperfStateWrapper::send_summary_report(float start, float end, char sent_bytes[], char bandwidth[]) {
    _test_state.jniCallback.on_result(&_test_state, start, end, sent_bytes, bandwidth);
}
