package com.lcl.lclmeasurementtool.model.datamodel

import com.google.protobuf.ByteString

data class UserData(
    val showData: Boolean,
    val loggedIn: Boolean,
    val hPKR: ByteString,
    val skT: ByteString
)