package com.jetbrains.entrytasks.fileindexer.lib

import java.io.File
import java.io.InputStream
import java.io.StreamTokenizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by gabriel on 24.07.18.
 */


/**
 * Main class that processes specified list of files and directories building token index incrementally and in parallel.
 * TODO:
 * - exception handling
 * - configuration options for whitespace/token characters
 */
class Indexer(private val roots: List<File>) {
    private val _index: RWIndex = ConcurrentHashMapIndex()
    val index: Index get() = _index

    private val pool = Executors.newWorkStealingPool()
    private var nrunning = AtomicLong(0)

    init {
        for (f in roots) {
            if (!f.canRead()) throw IllegalArgumentException("Cannot access specified path: " + f.absolutePath)
        }
    }

    fun startIndexing() {
        for (root in roots) {
            if (root.isDirectory) {
                for (f in root.walkBottomUp().filter { it.isFile }) {
                    pool.execute {
                        processFile(f)
                        nrunning.decrementAndGet()
                    }
                    nrunning.incrementAndGet()
                }
            } else {
                pool.execute {
                    processFile(root)
                    nrunning.decrementAndGet()
                }
                nrunning.incrementAndGet()
            }
        }
    }

    fun stopIndexing() {
        pool.shutdownNow()
    }

    fun isRunning(): Boolean = nrunning.get() > 0

    // TODO: onCompleted

    // runs on a thread pool concurrently with other files
    private fun processFile(f: File) {
        processStream(f.path, f.inputStream(), _index)
    }
}

// for testing: it's easier to supply fake stream than fake file
fun processStream(path: String, stream: InputStream, index: RWIndex) {
    stream.buffered().reader().use {
        val tokenizer = StreamTokenizer(it)
        tokenizer.lowerCaseMode(true)
        do {
            tokenizer.nextToken()
            if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                index.insert(tokenizer.sval, LineInfo(path, tokenizer.lineno()))
            }
        } while (tokenizer.ttype != StreamTokenizer.TT_EOF)
    }
}
