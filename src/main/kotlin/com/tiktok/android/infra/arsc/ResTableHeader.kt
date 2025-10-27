package com.tiktok.android.infra.arsc

data class ResTableHeader(
    val chunkerHeader: ResChunkHeader,
    val packageCount: Long // 4 bytes
 )