package com.jetbrains.entrytasks.fileindexer.lib

/**
 * LineInfo describes position of search term in a file.
 */
data class LineInfo(val fileName: String, val lineNumber: Int) : Comparable<LineInfo> {
    override fun compareTo(other: LineInfo): Int {
        val c = fileName.compareTo(other.fileName)
        if (c == 0) return lineNumber.compareTo(other.lineNumber)
        return c
    }

}
