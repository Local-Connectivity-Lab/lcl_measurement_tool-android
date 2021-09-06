#include <android/log.h>

#include "iperf3_state_wrapper.h"
#include "iperf_api.h"

#include <vector>

int
stop_wrapper(struct iperf_test_state *test_wrapper) {
    // TODO(matt9j) This can be called from a concurrent context to run_wrapper, and cause race
    //  conditions or failures. The standalone iperf binary relies on a single threaded client and
    //  the OS signal taking over the single threaded context, which is not the case here.
    test_wrapper->iperf_test->done = 1;
    return 0;
}

int
run_wrapper(struct iperf_test_state *test_wrapper)
{
    /* Ignore SIGPIPE to simplify error handling */
    signal(SIGPIPE, SIG_IGN);

    struct iperf_test * test = test_wrapper->iperf_test;
    switch (test->role) {
        case 's':
            __android_log_print(ANDROID_LOG_ERROR, "lcl_meas", "Instructed to start an unsupported server!");
            break;
//            if (test->daemon) {
//                int rc;
//                rc = daemon(0, 0);
//                if (rc < 0) {
//                    i_errno = IEDAEMON;
//                    iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
//                }
//            }
//            if (iperf_create_pidfile(test) < 0) {
//                i_errno = IEPIDFILE;
//                iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
//            }
//            for (;;) {
//                int rc;
//                rc = iperf_run_server(test);
//                test->server_last_run_rc =rc;
//                if (rc < 0) {
//                    iperf_err(test, "error - %s", iperf_strerror(i_errno));
//                    if (test->json_output) {
//                        if (iperf_json_finish(test) < 0)
//                            return -1;
//                    }
//                    iflush(test);
//
//                    if (rc < -1) {
//                        iperf_errexit(test, "exiting");
//                    }
//                }
//                iperf_reset_test(test);
//                if (iperf_get_test_one_off(test) && rc != 2) {
//                    /* Authentication failure doesn't count for 1-off test */
//                    if (rc < 0 && i_errno == IEAUTHTEST) {
//                        continue;
//                    }
//                    break;
//                }
//            }
//            iperf_delete_pidfile(test);
//            break;
        case 'c':
            __android_log_print(ANDROID_LOG_VERBOSE, "lcl_meas", "Running client in run_wrapper");
            if (iperf_create_pidfile(test) < 0) {
                __android_log_print(ANDROID_LOG_ERROR, "lcl_meas", "Failed to create pidfile");
                i_errno = IEPIDFILE;
                iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
            }
            if (iperf_run_client(test) < 0) {
                __android_log_print(ANDROID_LOG_ERROR, "lcl_meas", "Failed to run client");
                iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
            }
            iperf_delete_pidfile(test);
            break;
        default:
            // TODO(matt9j) Can probably be eliminated???
            __android_log_print(ANDROID_LOG_ERROR, "lcl_meas", "Fell through to usage case");
            usage();
            break;
    }

    // TODO(matt9j) Can be removed if above is removed
//    iperf_catch_sigend(SIG_DFL);
    signal(SIGPIPE, SIG_DFL);

    return 0;
}