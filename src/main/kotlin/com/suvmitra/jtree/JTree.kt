package com.suvmitra.jtree

import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess


private const val ANSI_RESET = "\u001B[0m"
private const val ANSI_BLACK = "\u001B[30m"
private const val ANSI_RED = "\u001B[31m"
private const val ANSI_GREEN = "\u001B[32m"
private const val ANSI_YELLOW = "\u001B[33m"
private const val ANSI_BLUE = "\u001B[34m"
private const val ANSI_PURPLE = "\u001B[35m"
private const val ANSI_CYAN = "\u001B[36m"
private const val ANSI_WHITE = "\u001B[37m"
private const val SPACE = "    "

fun main(args: Array<String>) {

    // by default hidden file/folder will not be visited
    val walkHidden = System.getProperty("show-hidden", false.toString()).toBoolean()
    // by default symbolic link will not be visited
    val walkSymLink = System.getProperty("follow-symlink", false.toString()).toBoolean()
    // by default only directory visit will be false
    val walkOnlyDir = System.getProperty("only-dir", false.toString()).toBoolean()
    // by default walk till the leaf level
    val walkTillLevel = System.getProperty("walk-level", Int.MAX_VALUE.toString()).toInt()

    if(args.isEmpty()) {
        println("Please provide at least one directory as argument!\n")
        exitProcess(1)
    }

    val walkOptions = ArrayList<TreeOptions>()
    if(walkHidden) walkOptions.add(TreeOptions.WALK_HIDDEN)
    if(walkSymLink) walkOptions.add(TreeOptions.WALK_SYM_LINK)
    if(walkOnlyDir) walkOptions.add(TreeOptions.WALK_ONLY_DIR)

    args.forEach {
        val m = JTree()
        m.traverseDir(it, walkTillLevel, *walkOptions.toTypedArray()) // traverse each directory
        println(m.treeOutput)
        println(m.treeOutputMeta)
    }
}

class JTree {

    private var numDir = -1
    private var numFile = 0
    private var numSymLink = 0

    // output variables
    val treeOutput = StringBuilder()
    val treeOutputMeta = StringBuilder()

    private var walkTillLevel = Int.MAX_VALUE

    fun traverseDir(dirName: String, walkTillLevel: Int = Int.MAX_VALUE, vararg options: TreeOptions) {
        val dirPath: Path = Path.of(dirName)
        this.walkTillLevel = walkTillLevel

        // check if the path exists
        if (!Files.exists(dirPath)) {
            System.err.println("Err: This path does not exist: $dirName")
            exitProcess(1)
        }

        // check if this is a directory
        if (!Files.isDirectory(dirPath)) {
            System.err.println("Err: This is not a directory: $dirName")
            exitProcess(1)
        }

        // walk the directory and print
        fileWalk(dirPath, "", "", 0, *options)

        // remove empty lines
        val tempOutput = StringBuilder()
        tempOutput.append(treeOutput)
        treeOutput.clear()
        treeOutput.append(tempOutput.replace(Regex.fromLiteral("(?m)^\\s+\$"),""))

        treeOutputMeta.append("$numDir ${if (numDir > 1) "directories" else "directory"}, $numFile ${if (numFile > 1) "files" else "file"}, " +
                "$numSymLink ${if (numSymLink > 1) "symlinks" else "symlink"}")
    }

    private fun fileWalk(path: Path, space: String, prefix: String, level: Int, vararg options: TreeOptions) {
        val walkHidden = options.contains(TreeOptions.WALK_HIDDEN)
        val walkSymLink = options.contains(TreeOptions.WALK_SYM_LINK)
        val walkOnlyDir = options.contains(TreeOptions.WALK_ONLY_DIR)
        val toPrint = StringBuilder()

        if(level > walkTillLevel) return

        // do not print hidden files/dir if the option is not provided
        // go ahead if it is the root
        if (prefix != "" && path.toFile().name.startsWith(".") && !walkHidden) return

        if (Files.isSymbolicLink(path)) {
            numSymLink++
            // do no traverse symbolic link if option is not provided
            if (!walkSymLink) {
                toPrint.append("${if (space.startsWith(SPACE)) space.substring(SPACE.length) else space}$prefix $ANSI_CYAN${path.fileName}$ANSI_RESET -> ")
                if (path.toRealPath().toFile().isDirectory)
                    toPrint.append("$ANSI_BLUE${path.toRealPath()}$ANSI_RESET").append("\n")
                else
                    toPrint.append("$ANSI_GREEN${path.toRealPath()}$ANSI_RESET").append("\n")
                treeOutput.append(toPrint)
                return
            }
        }

        if (Files.isDirectory(path)) {
            numDir++
            if (Files.isSymbolicLink(path)) {
                toPrint.append("${if (space.startsWith(SPACE)) space.substring(SPACE.length) else space}$prefix $ANSI_CYAN${path.fileName}$ANSI_RESET -> ")
                if (path.toRealPath().toFile().isDirectory)
                    toPrint.append("$ANSI_BLUE${path.toRealPath()}$ANSI_RESET").append("\n")
                else
                    toPrint.append("$ANSI_GREEN${path.toRealPath()}$ANSI_RESET").append("\n")
            } else {
                toPrint.append(
                    "${if (space.startsWith(SPACE)) space.substring(SPACE.length) else space}$prefix${
                        if (prefix == "") ""
                        else " "
                    }$ANSI_BLUE${if (prefix == "") path else path.fileName}$ANSI_RESET"
                ).append("\n")
            }
            treeOutput.append(toPrint)

            var k = 0
            path.toFile().listFiles()?.sorted()?.forEach {
                if (++k == path.toFile().listFiles()?.size)
                    if (prefix == "├──")
                        fileWalk(it.toPath(), "$space│$SPACE", "└──", level+1, *options)
                    else
                        fileWalk(it.toPath(), "$space$SPACE", "└──", level+1, *options)
                else
                    if (prefix == "├──")
                        fileWalk(it.toPath(), "$space│$SPACE", "├──", level+1, *options)
                    else
                        fileWalk(it.toPath(), "$space$SPACE", "├──", level+1, *options)
            }
        } else {
            if(walkOnlyDir)
                return
            numFile++
            if(Files.probeContentType(path) == null)
                toPrint.append(
                    "${
                        if (space.startsWith(SPACE)) space.substring(SPACE.length)
                        else space
                    }$prefix $ANSI_GREEN${path.fileName}$ANSI_RESET"
                ).append("\n")
            else if (Files.probeContentType(path).startsWith("image"))
                toPrint.append(
                    "${
                        if (space.startsWith(SPACE)) space.substring(SPACE.length)
                        else space
                    }$prefix $ANSI_PURPLE${path.fileName}$ANSI_RESET"
                ).append("\n")
            else if (Files.probeContentType(path).startsWith("application"))
                toPrint.append(
                    "${
                        if (space.startsWith(SPACE)) space.substring(SPACE.length)
                        else space
                    }$prefix $ANSI_RED${path.fileName}$ANSI_RESET"
                ).append("\n")
            else
                toPrint.append(
                    "${
                        if (space.startsWith(SPACE)) space.substring(SPACE.length)
                        else space
                    }$prefix $ANSI_GREEN${path.fileName}$ANSI_RESET"
                ).append("\n")
            treeOutput.append(toPrint)
        }
    }
}