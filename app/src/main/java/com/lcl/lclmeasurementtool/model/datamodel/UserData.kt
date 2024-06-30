package com.lcl.lclmeasurementtool.model.datamodel

import com.google.protobuf.ByteString

data class UserData(
    val deviceID: String,
    val showData: Boolean,
    val loggedIn: Boolean,
    val hPKR: ByteString,
    val skT: ByteString,
    val R: ByteString
)