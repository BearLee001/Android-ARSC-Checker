package com.tiktok.android.infra.arsc

data class ResStringPoolHeader(
    val resChunkHeader: ResChunkHeader,
    val stringCount: Long, // 4 bytes
    val styleCount: Long, // 4 bytes
    val tag: Long, // 4 bytes
    val stringStartOffset: Long, // 4 bytes
    val styleStartOffset: Long, // 4 bytes
) {
    override fun toString(): String {
        return "ResStringPoolHeader(blockSize=${resChunkHeader.size.toMiB()} MiB, stringCount=$stringCount, styleCount=$styleCount)"
    }
}