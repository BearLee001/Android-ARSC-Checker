package com.tiktok.android.infra.arsc

const val COMPLEX_ENTRY = 1
const val ENTRY = 0

/* Note:
 *  flag -> 1: entry == null entryMap != null
 *  flag -> 0: entry != null entryMap == null
 */
data class ResTableEntry(
    val flag: Int = 0,
    val key: Int,
    val entry: Entry?,
    val entryMap: Map<Int, Set<Entry>>?
    )

data class Entry(
    val type: Int,
    val data: Int
)