package com.tiktok.android.infra.arsc

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteBuffer.read2Bytes(): Int {
    this.order(ByteOrder.LITTLE_ENDIAN)
    return this.short.toInt() and 0xFFFF
}

fun ByteBuffer.read4Bytes(): Long {
    this.order(ByteOrder.LITTLE_ENDIAN)
    return this.int.toLong()
}

fun ByteBuffer.readAndGetBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    this.get(bytes)
    return bytes
}

fun Long.toMiB(): String {
    return "${this.toDouble() / 1000.0 / 1000.0 }"
}

fun ByteBuffer.peek2Bytes() : Int {
    return this.get(this.position()) + this.get(this.position() + 1) * 256
}