package com.lcl.lclmeasurementtool.features.ping

data class PingPacket(
    val magic: Int = PingCodec.MAGIC,
    val version: UByte = PingCodec.VERSION,
    val requestId: Long,
    val sequence: Int,
    val clientSendTimestamp: Long,
    val serverReceiveTimestamp: Long? = null,
    val serverSendTimestamp: Long? = null,
)
