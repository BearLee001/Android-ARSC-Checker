package com.tiktok.android.infra.arsc

data class ResPackageHeader(
    val resChunkHeader: ResChunkHeader,
    val packageId: Long, // 4 bytes
    val packageName: ByteArray, // 256 bytes
    val typeStringPoolOffset: Long, // 4 bytes
    val lastPublishType: Long, // 4 bytes
    val keywordStringPoolOffset: Long, // 4 bytes
    val lastPublishKey: Long, // 4 bytes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResPackageHeader

        if (resChunkHeader.type != other.resChunkHeader.type) return false
        if (resChunkHeader.headerSize != other.resChunkHeader.headerSize) return false
        if (resChunkHeader.size != other.resChunkHeader.size) return false
        if (packageId != other.packageId) return false
        if (!packageName.contentEquals(other.packageName)) return false

        return true
    }

    override fun hashCode(): Int {
        var result: Int = resChunkHeader.type
        result = 31 * result + resChunkHeader.headerSize
        result = 31 * result + resChunkHeader.size.toInt()
        result = 31 * result + packageId.toInt()
        result = 31 * result + packageName.contentHashCode()
        return result
    }
    override fun toString(): String {
        return "ResPackage(packageType=${resChunkHeader.type}, " +
                "headerSize=${resChunkHeader.headerSize}, " +
                "blockSize=${resChunkHeader.size}, "
        // Or omit body entirely: "ResPackage(header=$header, typeStringPool=$typeStringPool, nameStringPool=$nameStringPool)"
    }
}
