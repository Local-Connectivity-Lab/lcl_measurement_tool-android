package com.lcl.lclmeasurementtool.features.ping

data class UdpPingPacket(
    val magic: Int = UdpPingCodec.MAGIC,
    val version: UByte = UdpPingCodec.VERSION,
    val requestId: Long,
    val sequence: Int,
    val clientSendTimestamp: Long,
    val serverReceiveTimestamp: Long? = null,
    val serverSendTimestamp: Long? = null,
)
