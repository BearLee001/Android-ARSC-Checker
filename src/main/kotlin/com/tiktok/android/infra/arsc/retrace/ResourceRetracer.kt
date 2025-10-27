package com.tiktok.android.infra.arsc.retrace

import java.io.File

class ResourceRetracer(mapping: File, obfuscatedKeys: List<String>) {
    constructor(path: String, names: List<String>) : this(File(path), names)
    private val mapper: Map<String, String> by lazy {
        mapping.readLines()
            .asSequence()
            .map(String::trim)
            .filter {it.contains("R.string")}
            .associate { line ->
                val parts = line.split("->").map(String::trim)
                    .also { if (it.size != 2) throw IllegalArgumentException("Invalid mapping line: $line") }
                val originalKey = parts[0].substringAfterLast('.').trim()
                val obfuscatedKey = parts[1].substringAfterLast('.').trim()
                obfuscatedKey to originalKey
            }
    }

    private val obfuscatedKeys: List<String> = obfuscatedKeys.toList()

    fun retrace(): RetraceResult {
        val (found, missings) = obfuscatedKeys.partition { it in mapper }
        // Note:
        //  two cases where the key cannot be deobfuscated
        //  1. the key is not deobfuscated
        //  2. type of the key is not string
        val originalKeys = found.map { mapper.getValue(it) } + missings
        return RetraceResult(originalKeys, missings)
    }
}

data class RetraceResult(
    val originalKeys: List<String>,
    val failures: List<String>,
)