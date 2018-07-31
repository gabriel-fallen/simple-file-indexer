package com.jetbrains.entrytasks.fileindexer.lib

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ConcurrentHashMapIndexTest {
    @Test fun readOwnWrites() {
        val index = ConcurrentHashMapIndex()
        val li = LineInfo("dummy", 42)

        index.insert("test", li)
        var res = index.lookup("test")

        assertTrue(res.isPresent,"Something's there")
        assertEquals(li, res.get().next())

        index.insert("foo", li)
        res = index.lookup("foo")

        assertTrue(res.isPresent,"Something's there")
        assertEquals(li, res.get().next())

        val li2 = LineInfo("dummy2", 11)

        index.insert("foo", li2)
        res = index.lookup("foo")

        assertTrue(res.isPresent,"Something's there")
        assertEquals(listOf(li, li2), res.get().asSequence().toList())
    }

    @Test fun noWriteNoRead() {
        val index = ConcurrentHashMapIndex()

        var res = index.lookup("foo")
        assertFalse(res.isPresent,"Something's there")

        res = index.lookup("bar")
        assertFalse(res.isPresent,"Something's there")

        res = index.lookup("foobar")
        assertFalse(res.isPresent,"Something's there")

        // what if it somehow appeared after the first lookup?
        res = index.lookup("foo")
        assertFalse(res.isPresent,"Something's there")
    }
}
