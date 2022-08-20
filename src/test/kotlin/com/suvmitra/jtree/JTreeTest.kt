package com.suvmitra.jtree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JTreeTest {

    @Test
    fun test1_all_file_and_dir() {
        val jTree = JTree()
        jTree.traverseDir("src/test")
        val out = jTree.outputMeta
        assertEquals("4 directories, 1 file, 0 symlink", out.toString())
    }

    @Test
    fun test2_walk_till_level_1() {
        val jTree = JTree()
        jTree.traverseDir("src/test", 1)
        val out = jTree.outputMeta
        assertEquals("1 directory, 0 file, 0 symlink", out.toString())
    }
}