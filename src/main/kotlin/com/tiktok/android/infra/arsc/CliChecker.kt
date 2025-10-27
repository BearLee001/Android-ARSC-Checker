package com.tiktok.android.infra.arsc

import java.io.File
import java.nio.ByteBuffer

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Error: No ARSC file path provided.")
        println("Usage: java -jar your-jar-name.jar <path/to/file1.arsc> [path/to/file2.arsc] ...")
        return
    }

    resolveArscs(args.toList())
}

fun resolveArscs(arscPaths: List<String>) {
    arscPaths.forEach { arscPath ->
        println("\nProcessing: $arscPath")

        val file = File(arscPath)
        if (!file.exists()) {
            println("-> Failure: File not found.")
            return@forEach
        }

        try {
            val parser = ARSCParser(ByteBuffer.wrap(file.readBytes()))
            val arsc = parser.parse()
            println("-> Success: Parsed ${arsc.packages.size} package(s) with ${arsc.stringPool.strings.size} global strings.")
        } catch (e: Exception) {
            println("-> Failure: An error occurred during parsing.")
            println("   Reason: ${e.message}")
        }
    }
}
