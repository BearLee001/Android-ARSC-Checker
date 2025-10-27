package com.tiktok.android.infra.arsc

data class ResTypeSpecHeader(
    val type: Int, // 2 bytes
    val headerSize: Int, // 2 bytes
    val blockSize: Long, // 4 bytes
    val typeId: Int, // 1 byte
    val reserved: ByteArray, // 3 bytes
    val resSpecCount: Long, // 4 bytes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResTypeSpecHeader

        if (type != other.type) return false
        if (headerSize != other.headerSize) return false
        if (blockSize != other.blockSize) return false
        if (typeId != other.typeId) return false
        if (!reserved.contentEquals(other.reserved)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + headerSize
        result = 31 * result + blockSize.toInt()
        result = 31 * result + typeId
        result = 31 * result + reserved.contentHashCode()
        return result
    }
}