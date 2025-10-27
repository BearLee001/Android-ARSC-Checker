package com.tiktok.android.infra.arsc

data class ResConfig(
    val resChunkHeader: ResChunkHeader,
    val typeId: Int, // 1 byte
    val reserved: ByteArray, // 3 bytes
    val resGroupCount: Long, // 4 bytes
    val tableEntryStartOffset: Long, // 4 bytes
    val configFlag: ResConfigFlags, // note: variable
    val tableEntryOffsets: IntArray,
    val tableEntries: List<ResTableEntry>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResConfig

        if (resChunkHeader.type != other.resChunkHeader.type) return false
        if (resChunkHeader.headerSize != other.resChunkHeader.headerSize) return false
        if (resChunkHeader.size != other.resChunkHeader.size) return false
        if (typeId != other.typeId) return false
        if (!reserved.contentEquals(other.reserved)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resChunkHeader.type
        result = 31 * result + resChunkHeader.headerSize
        result = 31 * result + resChunkHeader.size.toInt()
        result = 31 * result + typeId
        result = 31 * result + reserved.contentHashCode()
        return result
    }
}