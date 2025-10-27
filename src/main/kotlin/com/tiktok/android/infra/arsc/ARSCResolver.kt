package com.tiktok.android.infra.arsc

import com.tiktok.android.infra.arsc.retrace.ResourceRetracer
import java.io.File
import java.nio.ByteBuffer

class ARSCResolver(val arsc: ARSC, val path: String, val mapping: String) {
    companion object {
        const val ERROR_OFFSET = 17
    }
    fun resolve(pkgName: String? = null) {
        println("begin resolve arsc...")
        println("stringPoolSize = ${arsc.stringPool.header.resChunkHeader.size.toDouble() / 1000.0 / 1000.0} MiB")
        println("stringPoolSize = ${arsc.stringPool.strings.size}")
        println("arsc has ${arsc.packages.size} packages:")
        arsc.packages
            .filter { pkgName == null || resolvePkgName(it.header.packageName) == pkgName }
            .forEach { pkg ->
                println("package name: ${resolvePkgName(pkg.header.packageName)}")
                println("package info: $pkg")
                resolvePkgNameStringPool(pkg, path, mapping)
                println("package size = ${pkg.header.resChunkHeader.size.toDouble() / 1000.0 / 1000.0} MiB")
                val errorTypeSpec = pkg.typeSpecs[ERROR_OFFSET]
                println("config size = ${errorTypeSpec.configs.size}")
                println("tableEntryOffsets: ${errorTypeSpec.configs.first().tableEntryOffsets.size}")
                var tableEntrySize = 0
                errorTypeSpec.configs.forEach { config ->
                    tableEntrySize += config.tableEntries.size
                }
                println("tableEntry size = $tableEntrySize")
            }
        println()
    }

    private fun resolvePkgName(name: ByteArray) : String {
        val builder = StringBuilder()
        val bytes = ByteBuffer.wrap(name)
        while (true) {
            val byte = bytes.get()
            if (byte == 0.toByte()) {
                return builder.toString()
            }
            builder.append(byte.toInt().toChar())
            bytes.get()
        }
    }

    fun resolveStrings(output: String? = null, mapping: String? = null, ) {
        val names = mutableListOf<String>()
        arsc.packages.forEach { pkg ->
            val keys = pkg.nameStringPool.strings
            keys.forEach { key ->
                val bytes = ByteBuffer.wrap(key)
                /*
                 I don't know what the first two bytes means...
                 */
//                val firstByte = bytes.get()
//                val secondByte = bytes.get()
//                if (firstByte != secondByte) {
//                    throw RuntimeException("Unexpected string item")
//                }
                bytes.position(UNKNOW_BYTE_SIZE)
                // ignore the last '\0'
                val name = bytes
                    .readAndGetBytes(key.size - (UNKNOW_BYTE_SIZE + LAST_ZERO_SIZE))
                    .toString(Charsets.UTF_8)
                names.add(name)
            }
        }
        /* Note:
         *  'names' contains all types: id, style... not only string, but the comparing only cares string.
         *   That's why some of names can't be retrace. So just ignore only-remove.txt
         */
        if (output != null) {
            File(output).bufferedWriter().use { writer ->
                (mapping?.let { ResourceRetracer(it, names.distinct()).retrace().originalKeys } ?: names)
                    .joinTo(writer, separator = "\n", postfix = "\n")
            }
        } else {
            (mapping?.let { ResourceRetracer(it, names.distinct()).retrace().originalKeys } ?: names).forEach(::println)
        }
    }

    fun resolvePkgNameStringPool(pkg: ResPackage, output: String? = null, mapping: String? = null) {
        val names = mutableListOf<String>()
        val keys = pkg.nameStringPool.strings
        keys.forEach { key ->
            val bytes = ByteBuffer.wrap(key)
            /*
             I don't know what the first two bytes means...
             */
//                val firstByte = bytes.get()
//                val secondByte = bytes.get()
//                if (firstByte != secondByte) {
//                    throw RuntimeException("Unexpected string item")
//                }
            bytes.position(UNKNOW_BYTE_SIZE)
            // ignore the last '\0'
            val name = bytes
                .readAndGetBytes(key.size - (UNKNOW_BYTE_SIZE + LAST_ZERO_SIZE))
                .toString(Charsets.UTF_8)
            names.add(name)
        }
        /* Note:
         *  'names' contains all types: id, style... not only string, but the comparing only cares string.
         *   That's why some of names can't be retrace. So just ignore only-remove.txt
         */
        if (output != null) {
            File(output).bufferedWriter().use { writer ->
                (mapping?.let { ResourceRetracer(it, names.distinct()).retrace().originalKeys } ?: names)
                    .joinTo(writer, separator = "\n", postfix = "\n")
            }
        } else {
            (mapping?.let { ResourceRetracer(it, names.distinct()).retrace().originalKeys } ?: names).forEach(::println)
        }
    }
}
const val UNKNOW_BYTE_SIZE = 2
const val LAST_ZERO_SIZE = 1
