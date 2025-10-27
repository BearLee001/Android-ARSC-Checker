package com.tiktok.android.infra.arsc

data class ResChunkHeader(
    val type: Int,       // 2 bytes
    val headerSize: Int, // 2 bytes
    val size: Long,        // 4 bytes
)