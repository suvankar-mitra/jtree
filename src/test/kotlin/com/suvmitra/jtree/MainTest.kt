package com.suvmitra.jtree

import org.junit.jupiter.api.Test

class MainTest {

    @Test
    fun test1() {
        val m = Main()
        m.traverseDir("src/test")
        val out = m.outputMeta
        assert(out.toString() == "4 directories, 1 file, 0 symlink")
    }
}