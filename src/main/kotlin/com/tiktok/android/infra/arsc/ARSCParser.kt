package com.tiktok.android.infra.arsc

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/* Note:
 *  This code refers to https://github.com/shwenzhang/AndResGuard/blob/master/AndResGuard-core/src/main/java/com/tencent/mm/androlib/res/decoder/RawARSCDecoder.java
 *  but easier
 */
class ARSCParser(val buffer: ByteBuffer, val ord: ByteOrder = ByteOrder.LITTLE_ENDIAN) {

    constructor(arscPath: String, ord: ByteOrder = ByteOrder.LITTLE_ENDIAN)
            : this(ByteBuffer.wrap(File(arscPath).readBytes()))

    companion object {
        const val TYPE_TYPE = 0x0201
        const val TYPE_SPEC_TYPE = 0x0202
        const val TYPE_LIBRARY = 0x0203
        const val ENTRY_FLAG_COMPLEX = 0x0001
    }

    var counter = 0
    /**
     * resolve ARSC
     */
    fun parse(): ARSC {
        val header = parseResTableHeader()
        val globalStringPool = parseStringPool()

        val packages = mutableListOf<ResPackage>()
        repeat(header.packageCount.toInt()) {
            val resPackage = parsePackage()
            packages.add(resPackage)
        }

        return ARSC(header, globalStringPool, packages)
    }
    /**
     * resolve ARSC header (12 bytes)
     */
    fun parseResTableHeader(): ResTableHeader {
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        return ResTableHeader(
            ResChunkHeader(
                type = buffer.read2Bytes(),
                headerSize = buffer.read2Bytes(),
                size = buffer.int.toLong() and 0xFFFFFFFFL
            ),
            packageCount = buffer.read4Bytes()
        )
    }

    /**
     * resolve a pool string (ResStringPool)
     */
    fun parseStringPool(): ResStringPool {
        // record the starting position
        val chunkStartPosition = buffer.position()

        // 1. resolve the string pool header
        val header = parseStringPoolHeader()

        // 2. resolve offset array
        buffer.position(chunkStartPosition + header.resChunkHeader.headerSize)
        val stringOffsets = parseIntArray(header.stringCount.toInt())
        val styleOffsets = parseIntArray(header.styleCount.toInt())

        // 3. compute the absolute position of strings area
        // 3.1 compute the absolute starting position of strings area
        val stringsContentStart = chunkStartPosition + header.stringStartOffset

        // 3.2 compute the ending position of strings area
        // a) if the count of styles is greater than zero, then the position is the starting position of styles area
        // b) else the ending position is the block ending position
        val stringsContentEnd = if (header.styleCount > 0) {
            chunkStartPosition + header.styleStartOffset
        } else {
            chunkStartPosition + header.resChunkHeader.size.toInt()
        }

        // 4. resolve strings area
        val strings = parseContent(
            stringOffsets,
            stringsContentStart.toInt(),
            stringsContentEnd.toInt()
        )

        // 5. resolve styles area (if existed)
        val styles: List<ByteArray>
        if (header.styleCount > 0) {
            val stylesContentStart = chunkStartPosition + header.styleStartOffset
            val stylesContentEnd = chunkStartPosition + header.resChunkHeader.size.toInt()

            styles = parseContent(
                styleOffsets,
                stylesContentStart.toInt(),
                stylesContentEnd
            )
        } else {
            styles = emptyList()
        }

        return ResStringPool(header, stringOffsets, styleOffsets, strings, styles)
    }

    fun parsePackage() : ResPackage {
        // the starting position of package chunk
        val packageHeaderPosition = buffer.position()
        val header = parseResPackageHeader()

        // compute the absolute starting position of type & name string pool
        val typeStringStartPosition = packageHeaderPosition + header.typeStringPoolOffset.toInt()
        val nameStringStartPosition = packageHeaderPosition + header.keywordStringPoolOffset.toInt()
        val packageEndPosition = packageHeaderPosition + header.resChunkHeader.size.toInt()

        buffer.position(typeStringStartPosition)
        val typeStringPool = parseTypeStringPool()

        buffer.position(nameStringStartPosition)
        val nameStringPool = parseNameStringPool()

        val typeSpecs = parseTypeSpecs()
        val body = buffer.readAndGetBytes(packageEndPosition - buffer.position())
        return ResPackage(
            header = header,
            typeStringPool = typeStringPool,
            nameStringPool = nameStringPool,
            typeSpecs = typeSpecs,
            body = body,
        )
    }

    private fun parseResPackageHeader(): ResPackageHeader {
        return ResPackageHeader(
            ResChunkHeader(
                type = buffer.read2Bytes(),
                headerSize = buffer.read2Bytes(),
                size = buffer.read4Bytes(),
            ),
            packageId = buffer.read4Bytes(),
            packageName = buffer.readAndGetBytes(256),
            typeStringPoolOffset = buffer.read4Bytes(),
            lastPublishType = buffer.read4Bytes(),
            keywordStringPoolOffset = buffer.read4Bytes(),
            lastPublishKey = buffer.read4Bytes()
        )
    }

    private fun parseTypeStringPool(): ResTypeStringPool {
        return parseStringPool()
    }

    private fun parseNameStringPool(): ResNameStringPool {
        return parseStringPool()
    }

    private fun parseTypeSpecs(): List<ResTypeSpec> = generateSequence {
        if (buffer.peek2Bytes() != TYPE_SPEC_TYPE) null
        else if (buffer.peek2Bytes() != TYPE_LIBRARY) {
            val header = parseTypeSpecHeaderOnce()
            val configs = generateSequence {
                if (buffer.peek2Bytes() != TYPE_TYPE) null
                else {
                    parseConfig()
                }
            }.toList()
            ResTypeSpec(header, configs)
        } else {
            TODO("Only can prase chunk TYPE_SPEC_TYPE and TYPE_TYPE")
        }
    }.toList()

    private fun parseTypeSpecHeaderOnce() : ResTypeSpecHeader {
        val header = parseTypeSpecHeader()
        // Note: This is not used, just skip
        buffer.readAndGetBytes(header.resSpecCount.toInt() * 4)
        return header
    }


    private fun parseTypeSpecHeader(): ResTypeSpecHeader {
        return ResTypeSpecHeader(
            type = buffer.read2Bytes(),
            headerSize = buffer.read2Bytes(),
            blockSize = buffer.read4Bytes(),
            typeId = buffer.get().toInt(),
            reserved = buffer.readAndGetBytes(3),
            resSpecCount = buffer.read4Bytes())
    }

    private fun parseConfig(): ResConfig {
        /*
         * read config header
         */
        val configStartPosition = buffer.position()
        val resChunkHeader = ResChunkHeader(
            type = buffer.read2Bytes(),
            headerSize = buffer.read2Bytes(),
            size = buffer.read4Bytes(),
        )
        val typeId = buffer.get().toInt()
        val reserved = buffer.readAndGetBytes(3)
        val resGroupCount = buffer.read4Bytes()
        val tableEntryStartOffset = buffer.read4Bytes()
        val tableEntryStartPosition = configStartPosition + tableEntryStartOffset

        /*
         * read config flags
         */
        val configFlags = parseConfigFlags()

        /* Note:
         *  read table entry offset array
         *  Resource Shrinker provided by Android Gradle Plugin cannot shrink this array
         */
        val tableEntryOffsets = parseIntArray(resGroupCount.toInt())

        /*
         * read table entry
         */
        val entryTable = parseEntryTable(tableEntryOffsets, tableEntryStartPosition.toInt())
        val resConfig = ResConfig(resChunkHeader,
            typeId,
            reserved,
            resGroupCount,
            tableEntryStartOffset,
            configFlags,
            tableEntryOffsets,
            entryTable)

        return resConfig
    }

    /**
     * Note:
     *  This structure has variable length
     */
    private fun parseConfigFlags() : ResConfigFlags {
        var read = 28
        val size = buffer.read4Bytes()
        if (size < 28) {
            throw RuntimeException("Config size < 28")
        }
        val mcc = buffer.read2Bytes()
        val mnc = buffer.read2Bytes()
        val language = Pair(buffer.get(), buffer.get())
        val country = Pair(buffer.get(), buffer.get())
        val orientation = buffer.get()
        val touchscreen = buffer.get()
        val density = buffer.read2Bytes()
        val keyboard = buffer.get()
        val navigation = buffer.get()
        val inputFlags = buffer.get()
        /* inputPad0 */
        buffer.get()

        val screenWidth = buffer.read2Bytes()
        val screenHeight = buffer.read2Bytes()

        val sdkVersion = buffer.read2Bytes()
        /* minorVersion, now must always be 0 */
        buffer.read2Bytes()

        var screenLayout: Byte = 0
        var uiMode: Byte = 0
        var smallestScreenWidthDp: Int = 0


        if (size >= 32) {
            screenLayout = buffer.get()
            uiMode = buffer.get()
            smallestScreenWidthDp = buffer.read2Bytes()
            read = 32
        }

        var screenWidthDp = 0
        var screenHeightDp = 0
        if (size >= 36) {
            screenWidthDp = buffer.read2Bytes()
            screenHeightDp = buffer.read2Bytes()
            read = 36
        }

        var localeScript: ByteArray? = null
        var localeVariant: ByteArray? = null
        if (size >= 48) {
            localeScript = buffer.readAndGetBytes(4)
            localeVariant = buffer.readAndGetBytes(8)
            read = 48
        }

        var screenLayout2: Byte = 0
        if (size >= 52) {
            screenLayout2 = buffer.get()
            buffer.readAndGetBytes(3)
            read = 52
        }

        if (size >= 56) {
            buffer.readAndGetBytes(4)
            read = 56
        }

        if (size >= 64) {
            buffer.readAndGetBytes(8)
            read = 64
        }

        val exceedingSize = size - 64
        if (exceedingSize > 0) {
            TODO()
        } else {
            val remainingSize = size - read
            if (remainingSize > 0) {
                buffer.readAndGetBytes(remainingSize.toInt())
            }
        }
        // Note: We don't need config information, so this is not implemented!
        return ResConfigFlags(ByteArray(1))

    }

    /**
     * resolve string pool header (28 bytes)
     */
    private fun parseStringPoolHeader(): ResStringPoolHeader {
        return ResStringPoolHeader(
            ResChunkHeader(
                type = buffer.read2Bytes(),
                headerSize = buffer.read2Bytes(),
                size = buffer.int.toLong() and 0xFFFFFFFFL,
            ),
            stringCount = buffer.read4Bytes(),
            styleCount = buffer.read4Bytes(),
            tag = buffer.read4Bytes(),
            stringStartOffset = buffer.read4Bytes(),
            styleStartOffset = buffer.read4Bytes()
        )
    }

    /**
     * resolve offset array
     */
    private fun parseIntArray(count: Int): IntArray {
        val result = IntArray(count)
        repeat(count) { idx ->
            result[idx] = buffer.read4Bytes().toInt()
        }
        return result
    }

    /**
     * parse string or style array
     */
    private fun parseContent(
        offsets: IntArray,
        contentStartOffset: Int,
        contentEndOffset: Int
    ): List<ByteArray> {

        val result = mutableListOf<ByteArray>()

        for (i in 0 until offsets.size) {
            val itemStart = contentStartOffset + offsets[i]

            val itemEnd = if (i < offsets.size - 1) {
                contentStartOffset + offsets[i + 1]
            } else {
                contentEndOffset
            }

            val itemSize = itemEnd - itemStart

            buffer.position(itemStart)
            result.add(buffer.readAndGetBytes(itemSize))
        }
        return result
    }

    private fun parseEntryTable(
        offsets: IntArray,
        startPosition: Int,
    ) : List<ResTableEntry> {
        verifyAndSet(buffer, startPosition)
        val result = mutableListOf<ResTableEntry>()
        repeat(offsets.size) { idx ->
            /*
             * Note: 0xFFFFFFFF is invalid
             */
            if (offsets[idx] != -1) {
                result.add(readEntry())
            } else {
                counter += 1
            }
        }
        return result
    }

    private fun readEntry() : ResTableEntry {
        // size
        val sz = buffer.readAndGetBytes(2)

        val flags = buffer.read2Bytes()
        val specNameId = buffer.read4Bytes().toInt()

        return if ((flags and ENTRY_FLAG_COMPLEX) == 0) {
            ResTableEntry(ENTRY, specNameId, readValue(), null)
        } else {
            readComplexEntry(specNameId)
        }
    }

    private fun readValue() : Entry {
        // size
        val sz = buffer.read2Bytes()
        if (sz != 8) {
            TODO()
        }

        // zero
        val zero = buffer.get().toInt()
        require(zero == 0)

        val type = buffer.get().toInt()
        val data = buffer.read4Bytes().toInt()

        return Entry(type, data)
    }

    private fun readComplexEntry(key: Int) : ResTableEntry{
        // parent
        val parent = buffer.read4Bytes()
        val count = buffer.read4Bytes().toInt()

        val temp = mutableSetOf<Entry>()
        repeat(count) {
            buffer.read4Bytes()
            temp.add(readValue())
        }
        val map = mutableMapOf<Int, Set<Entry>>()
        map[parent.toInt()] = temp
        return ResTableEntry(COMPLEX_ENTRY, key, null, map)
    }

    private fun verifyAndSet(buffer: ByteBuffer, expected: Int) {
        if (buffer.position() != expected) {
            println("unexpected bytes...")
            buffer.position(expected)
        }
    }
}