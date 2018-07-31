package com.jetbrains.entrytasks.fileindexer.lib

import com.nhaarman.mockito_kotlin.*
import kotlin.test.Test


class TokenizerTest {
    private val testFilePath = "fake_file.test"

    @Test fun testSingleToken() {
        val index = mock<RWIndex>()

        processStream(testFilePath, "test".byteInputStream(), index)

        verify(index).insert("test", LineInfo(testFilePath, 1))
    }

    @Test fun testSingleTokens() {
        val index = mock<RWIndex>()

        processStream(testFilePath, "test foo bar".byteInputStream(), index)

        verify(index).insert("test", LineInfo(testFilePath, 1))
        verify(index).insert("foo", LineInfo(testFilePath, 1))
        verify(index).insert("bar", LineInfo(testFilePath, 1))
    }

    @Test fun testDoubleToken() {
        val index = mock<RWIndex>()

        processStream(testFilePath, "test foo test bar".byteInputStream(), index)

        verify(index, times(2)).insert("test", LineInfo(testFilePath, 1))
        verify(index).insert("foo", LineInfo(testFilePath, 1))
        verify(index).insert("bar", LineInfo(testFilePath, 1))
    }

    private fun runVerifyN(n: Int) {
        val index = mock<RWIndex>()

        for (i in 1..n)
            processStream(testFilePath, "test foo bar".byteInputStream(), index)

        verify(index, times(n)).insert("test", LineInfo(testFilePath, 1))
        verify(index, times(n)).insert("foo", LineInfo(testFilePath, 1))
        verify(index, times(n)).insert("bar", LineInfo(testFilePath, 1))
    }

    @Test fun testNTokens() {
        // property-testing for the poor :)
        for (n in 2..10)
            runVerifyN(n)
    }

    @Test fun testNegative() {
        val index = mock<RWIndex>()

        processStream(testFilePath, "test foo bar".byteInputStream(), index)

        verify(index).insert("test", LineInfo(testFilePath, 1))
        verify(index).insert("foo", LineInfo(testFilePath, 1))
        verify(index).insert("bar", LineInfo(testFilePath, 1))
        verify(index, times(0)).insert(argThat { "test" != this && "foo" != this && "bar" != this }, any())
    }
}
