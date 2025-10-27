package com.tiktok.android.infra.arsc

data class ResTypeSpec(
    val header: ResTypeSpecHeader,
    val configs: List<ResConfig>
)