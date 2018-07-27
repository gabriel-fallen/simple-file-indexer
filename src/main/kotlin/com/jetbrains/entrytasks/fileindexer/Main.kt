/**
 * Created by gabriel on 24.07.18.
 */

package com.jetbrains.entrytasks.fileindexer

import com.jetbrains.entrytasks.fileindexer.lib.Indexer
import java.awt.Dimension
import java.awt.EventQueue
import java.io.File
import javax.swing.*

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        val ui = IndexerGUI()
        ui.isVisible = true
    }
}


class IndexerGUI : JFrame() {
    private var indexer: Indexer? = null
    private val files: MutableList<File> = ArrayList()

    init {
        title = "Indexer"
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        createLayout()
    }

    private fun createLayout() {
        // initialize components
        val results    = JTextArea()
        val searchBox  = createSearchBox(results)
        val fileList   = JTextArea()
        val addFile    = createAddButton(fileList)
        val clearFiles = createClearButton(fileList)
        val stop       = createStopButton()
        val start      = createStartButton(stop)

        val quit = JButton("Quit")
        quit.addActionListener {
            System.exit(0)
        }

        val gl = GroupLayout(contentPane)
        contentPane.layout = gl

        gl.autoCreateContainerGaps = true
        gl.autoCreateGaps = true

        // single column
        // first row: search box
        // second row: results area, files area
        // third row: control buttons
        gl.setHorizontalGroup(gl.createParallelGroup()
                .addGroup(gl.createSequentialGroup()
                    .addComponent(searchBox))
                .addGroup(gl.createSequentialGroup()
                    .addComponent(fileList)
                    .addComponent(results))
                .addGroup(gl.createSequentialGroup()
                        .addComponent(addFile)
                        .addComponent(clearFiles)
                        .addComponent(quit)
                        .addComponent(stop)
                        .addComponent(start)))

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup()
                        .addComponent(searchBox))
                .addGroup(gl.createParallelGroup()
                        .addComponent(fileList)
                        .addComponent(results))
                .addGroup(gl.createParallelGroup()
                        .addComponent(addFile)
                        .addComponent(clearFiles)
                        .addComponent(quit)
                        .addComponent(stop)
                        .addComponent(start)))
    }

    private fun createStartButton(stop: JButton): JButton {
        val start = JButton("Start")

        start.addActionListener {
            indexer?.stopIndexing()
            indexer = Indexer(files)
            indexer?.startIndexing()
            stop.isEnabled = true
        }

        return start
    }

    private fun createStopButton(): JButton {
        val stop = JButton("Stop")
        stop.isEnabled = false

        stop.addActionListener {
            indexer?.stopIndexing()
            stop.isEnabled = false
        }

        return stop
    }

    private fun createAddButton(fileList: JTextArea): JButton {
        val add = JButton("Add files")

        add.addActionListener {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            chooser.isMultiSelectionEnabled = true
            val res = chooser.showOpenDialog(this)
            if (res == JFileChooser.APPROVE_OPTION) {
                for (file in chooser.selectedFiles) {
                    files.add(file)
                    fileList.append(file.canonicalPath + "\n")
                }
            }
        }

        return add
    }

    private fun createClearButton(fileList: JTextArea): JButton {
        val clear = JButton("Clear files")

        clear.addActionListener {
            files.clear()
            fileList.text = ""
        }

        return clear
    }

    private fun createSearchBox(results: JTextArea): JTextField {
        val searchBox = JTextField("Enter a term to search for and press 'enter'.")
        searchBox.maximumSize = Dimension(Int.MAX_VALUE, 50)

        searchBox.addActionListener {
            val term = searchBox.text
            val res = indexer?.index?.lookup(term)

            if (res == null) {
                results.text = "Search is possible only after you've started indexing."
                return@addActionListener
            }
            if (!res.isPresent) {
                results.text = "Nothing found."
                return@addActionListener
            }
            results.text = ""
            for (li in res.get()) {
                results.append(li.toString() + "\n")
            }
        }

        return searchBox
    }
}

private fun indexerCLI(args: Array<String>) {
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
