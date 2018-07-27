/**
 * Created by gabriel on 24.07.18.
 */

package com.jetbrains.entrytasks.fileindexer

import com.jetbrains.entrytasks.fileindexer.lib.Indexer
import java.io.File

fun main(args: Array<String>) {
    println("Indexer CLI")

    if (args.size < 2) {
        println("Usage: indexer term path [path...]")
        return
    }

    val term = args[0]
    val files = args.drop(1).map(::File)
    val indexer = Indexer(files)
    indexer.startIndexing()

    do {
        if (indexer.isRunning()) println("Still running")
        println("What to do?")
        print("(Quit/Print/Wait) > ")
        val input = readLine()?.trim()?.toLowerCase() ?: "q"
        if (input.startsWith('q')) break
        if (input.startsWith('p')) {
            indexer.index.lookup(term).ifPresent { it.forEach(::println) }
            continue // skip redundant if
        }
        if (input.startsWith('w')) {
            Thread.sleep(1000) // wait a second...
        }
    } while (true)
}
