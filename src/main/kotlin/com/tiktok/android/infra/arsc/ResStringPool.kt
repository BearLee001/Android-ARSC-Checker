package com.tiktok.android.infra.arsc

data class ResStringPool(
    val header: ResStringPoolHeader,
    val stringOffsets: IntArray,
    val styleOffsets: IntArray,
    val strings: List<ByteArray>,
    val styles: List<ByteArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResStringPool

        if (header != other.header) return false
        if (!stringOffsets.contentEquals(other.stringOffsets)) return false
        if (!styleOffsets.contentEquals(other.styleOffsets)) return false
        if (strings != other.strings) return false
        if (styles != other.styles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + stringOffsets.contentHashCode()
        result = 31 * result + styleOffsets.contentHashCode()
        result = 31 * result + strings.hashCode()
        result = 31 * result + styles.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResStringPool(header=$header)"
    }
}