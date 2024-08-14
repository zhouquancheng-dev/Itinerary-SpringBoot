package com.zqc.itineraryweb.entity.tim

data class UserSig(
    val sdkAppId: String,
    val userId: String,
    val expire: String,
    val userBuf: String? = null,
    val key: String
)
