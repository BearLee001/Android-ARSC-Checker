package com.tiktok.android.infra.arsc.compare

import java.io.File

class KeysComparer(val f1: File, val f2: File, val o1: File, val o2: File, val o3: File) {
    constructor(p1: String, p2: String, p3: String, p4: String, p5: String) :
            this(File(p1), File(p2), File(p3), File(p4), File(p5))

    fun compare() {
        val f1Lines = f1.readLines().toSet()
        val f2Lines = f2.readLines().toSet()

        val uniqueToF1 = f1Lines - f2Lines
        val uniqueToF2 = f2Lines - f1Lines
        val intersection = f1Lines intersect f2Lines

        o1.writeText(uniqueToF1.sorted().joinToString("\n"))
        o2.writeText(uniqueToF2.sorted().joinToString("\n"))
        o3.writeText(intersection.sorted().joinToString("\n"))
    }
}