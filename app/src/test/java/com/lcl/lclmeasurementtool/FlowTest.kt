//package com.lcl.lclmeasurementtool
//
//import com.lcl.lclmeasurementtool.features.iperf.IperfResult
//import com.lcl.lclmeasurementtool.features.iperf.IperfStatus
//import com.lcl.lclmeasurementtool.features.ping.PingError
//import com.lcl.lclmeasurementtool.features.ping.PingErrorCase
//import com.lcl.lclmeasurementtool.features.ping.PingResult
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.FlowPreview
//import kotlinx.coroutines.flow.asFlow
//import kotlinx.coroutines.flow.flatMapMerge
//import kotlinx.coroutines.flow.flattenMerge
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.runBlocking
//import org.junit.Test
//
//class FlowTest {
//
//    @OptIn(FlowPreview::class)
//    @Test
//    suspend fun testFlow() {
//
//        listOf<Int>(1,2,3).asFlow().flatMapMerge { listOf<Int>(4,5).asFlow() }.collect {
//            print(it)
//        }
//    }
//
//    @OptIn(FlowPreview::class)
//    fun main() = runBlocking {
//        flowOf(
//            flowOf(
//                IperfResult(123.123f, 123.124f, "100", "45.3", true, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "45.5", true, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "42.3", true, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "45.7", true, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "41.3", true, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "40.3", true, null, IperfStatus.FINISHED),
//            ),
//            flowOf(
//                IperfResult(123.123f, 123.124f, "100", "45.3", false, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "45.5", false, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "42.3", false, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "45.7", false, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "41.3", false, null, IperfStatus.RUNNING),
//                IperfResult(123.123f, 123.124f, "100", "40.3", false, null, IperfStatus.FINISHED),
//            ),
//        ).flattenMerge().collect {
//            print("${it.isDownMode} => ${it.bandWidth}")
//        }
//
//
//        flowOf(listOf<>.asFlow(), listOf<Int>(4,5).asFlow()).flattenMerge().collect {
//            print(it)
//        }
//    }
//}