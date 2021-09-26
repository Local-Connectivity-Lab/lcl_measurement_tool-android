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
};

extern std::mutex singleton_mutex;
extern IperfStateWrapper* global_state_wrapper;
extern bool global_stop_requested;

#endif //LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H
