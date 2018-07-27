package com.jetbrains.entrytasks.fileindexer.lib

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Created by gabriel on 24.07.18.
 */

/**
 * Read-only interface to an index.
 * Operation is expected to be thread-safe.
 */
interface Index {
    fun lookup(term: String): Optional<Iterator<LineInfo>>
}

/**
 * Read-write (internal) interface to an index.
 * Operations are expected to be thread-safe.
 */
interface RWIndex : Index {
    fun insert(term: String, line: LineInfo)
}

/**
 * Index implementation that wraps ConcurrentHashMap.
 */
class ConcurrentHashMapIndex : RWIndex {
    private val map = ConcurrentHashMap<String, ConcurrentSkipListSet<LineInfo>>()

    override fun insert(term: String, line: LineInfo) {
        map.computeIfAbsent(term, { ConcurrentSkipListSet() }).add(line)
    }

    override fun lookup(term: String): Optional<Iterator<LineInfo>> {
        return Optional.ofNullable(map[term]?.iterator())
    }
}

