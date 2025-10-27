package com.tiktok.android.infra.arsc

data class ARSC(
    val header: ResTableHeader,
    val stringPool: ResStringPool,
    val packages: List<ResPackage>,
)