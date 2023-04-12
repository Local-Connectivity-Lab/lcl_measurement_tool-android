@file:JvmName("Measurement")
package net.measurementlab.ndt7.android.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Measurement(
    @SerialName("ConnectionInfo") val connectionInfo: ConnectionInfo,
    @SerialName("BBRInfo") val bbrInfo: BBRInfo?,
    @SerialName("TCPInfo") val tcpInfo: TCPInfo?
)

@Serializable
data class ConnectionInfo(
    @SerialName("Client") val client: String,
    @SerialName("Server") val server: String,
    @SerialName("UUID") val uuid: String
)

@Serializable
data class BBRInfo(
    @SerialName("BW") val bw: Long?,
    @SerialName("MinRTT") val minRtt: Long?,
    @SerialName("PacingGain") val pacingGain: Long?,
    @SerialName("CwndGain") val cwndGain: Long?,
    @SerialName("ElapsedTime") val elapsedTime: Long?
)

@Serializable
data class TCPInfo(
    @SerialName("State") var state: Long?,
    @SerialName("CAState") val CaState: Long?,
    @SerialName("Retransmits") val retransmits: Long?,
    @SerialName("Probes") val probes: Long?,
    @SerialName("Backoff") val backoff: Long?,
    @SerialName("Options") val options: Long?,
    @SerialName("WScale") val wScale: Long?,
    @SerialName("AppLimited") val appLimited: Long?,
    @SerialName("RTO") val rto: Long?,
    @SerialName("ATO") val ato: Long?,
    @SerialName("SndMSS") val sndMss: Long?,
    @SerialName("RcvMSS") val rcvMss: Long?,
    @SerialName("Unacked") val unacked: Long?,
    @SerialName("Sacked") val sacked: Long?,
    @SerialName("Lost") val lost: Long?,
    @SerialName("Retrans") val retrans: Long?,
    @SerialName("Fackets") val fackets: Long?,
    @SerialName("LastDataSent") val lastDataSent: Long?,
    @SerialName("LastAckSent") val lastAckSent: Long?,
    @SerialName("LastDataRecv") val lastDataRecv: Long?,
    @SerialName("LastAckRecv") val lastAckRecv: Long?,
    @SerialName("PMTU") val pmtu: Long?,
    @SerialName("RcvSsThresh") val rcvSsThresh: Long?,
    @SerialName("RTT") val rtt: Long?,
    @SerialName("RTTVar") val rttVar: Long?,
    @SerialName("SndSsThresh") val sndSsThresth: Long?,
    @SerialName("SndCwnd") val sndCwnd: Long?,
    @SerialName("AdvMSS") val advMss: Long?,
    @SerialName("Reordering") val reordering: Long?,
    @SerialName("RcvRTT") val rcvRtt: Long?,
    @SerialName("RcvSpace") val rcvSpace: Long?,
    @SerialName("TotalRetrans") val totalRetrans: Long?,
    @SerialName("PacingRate") val pacingRate: Long?,
    @SerialName("MaxPacingRate") val maxPacingRate: Long?,
    @SerialName("BytesAcked") val bytesAcked: Long?,
    @SerialName("BytesReceived") val bytesReceived: Long?,
    @SerialName("SegsOut") val segsOut: Long?,
    @SerialName("SegsIn") val segsIn: Long?,
    @SerialName("NotsentBytes") val notSentBytes: Long?,
    @SerialName("MinRTT") val minRtt: Long?,
    @SerialName("DataSegsIn") val dataSegsIn: Long?,
    @SerialName("DataSegsOut") val dataSegsOut: Long?,
    @SerialName("DeliveryRate") val deliveryRate: Long?,
    @SerialName("BusyTime") val busyTime: Long?,
    @SerialName("RWndLimited") val rWndLimited: Long?,
    @SerialName("SndBufLimited") val sndBufLimited: Long?,
    @SerialName("Delivered") val delivered: Long?,
    @SerialName("DeliveredCE") val deliveredCE: Long?,
    @SerialName("BytesSent") val bytesSent: Long?,
    @SerialName("BytesRetrans") val bytesRetrans: Long?,
    @SerialName("DSackDups") val dSackDups: Long?,
    @SerialName("ReordSeen") val reordSeen: Long?,
    @SerialName("ElapsedTime") val elapsedTime: Long?
)
