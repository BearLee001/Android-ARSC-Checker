package com.tiktok.android.infra.arsc

fun main() {
    resolveArscs(listOf())
    return
}

fun resolveArscs(arscPaths: List<String>) {
    arscPaths.forEach { arscPath ->
        val parser = ARSCParser(arscPath)
        val arsc = parser.parse()
    }
}


