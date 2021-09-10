#ifndef LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H
#define LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H

#include <iperf_api.h>
#include <jni.h>
#include <mutex>

#include "iperf3_java_callback.h"
#include "iperf_state_wrapper_interface.h"

// C++ management class exposed above via iperf_state_wrapper_interface C API
class IperfStateWrapper {
public:
    IperfStateWrapper() noexcept;
    ~IperfStateWrapper() noexcept;
    bool register_callbacks(JNIEnv *javaEnv, jobject iperfConfig, jobject callback, jstring cacheDirTemplate);
    int run_test();
    int stop_test();
    void send_interval_report(float, float, char[], char[]);
    void send_summary_report(float, float, char[], char[]);
private:
    struct iperf_test_state _test_state;
    std::mutex _stop_signal_mutex;
    std::mutex _run_mutex;
};

#endif //LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H
