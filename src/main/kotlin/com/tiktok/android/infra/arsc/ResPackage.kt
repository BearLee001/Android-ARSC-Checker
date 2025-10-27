package com.tiktok.android.infra.arsc

data class ResPackage(
    val header: ResPackageHeader,
    val typeStringPool: ResTypeStringPool,
    val nameStringPool: ResNameStringPool,
    val typeSpecs: List<ResTypeSpec>,
    val body: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResPackage

        if (header != other.header) return false
        if (typeStringPool != other.typeStringPool) return false
        if (nameStringPool != other.nameStringPool) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + typeStringPool.hashCode()
        result = 31 * result + nameStringPool.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ResPackage(header=$header\ntypeStringPool=$typeStringPool\nnameStringPool=$nameStringPool\nbody=ByteArray(${body.size}))"
    }
}