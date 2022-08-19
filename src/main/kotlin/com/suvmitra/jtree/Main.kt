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

private var numDir = -1
private var numFile = 0
private var numSymLink = 0

fun main(args: Array<String>) {

    // by default hidden file/folder will not be visited
    val walkHidden = System.getProperty("show-hidden", false.toString()).toBoolean()
    // by default symbolic link will not be visited
    val walkSymLink = System.getProperty("follow-symlink", false.toString()).toBoolean()

    if(args.isEmpty()) {
        println("Please provide at least one directory as argument!\n")
        exitProcess(1)
    }

    val walkOptions = ArrayList<TreeOptions>()
    if(walkHidden) walkOptions.add(TreeOptions.WALK_HIDDEN)
    if(walkSymLink) walkOptions.add(TreeOptions.WALK_SYM_LINK)

    args.forEach {
        numDir = -1
        numFile = 0
        numSymLink = 0

        val m = Main()
        m.traverseDir(it, *walkOptions.toTypedArray()) // traverse each directory
        println(m.output)
        println(m.outputMeta)
    }
}

class Main {

    val output = StringBuilder()
    val outputMeta = StringBuilder()

    fun traverseDir(dirName: String, vararg options: TreeOptions) {
        val dirPath: Path = Path.of(dirName)

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
        fileWalk(dirPath, "", "", *options)

        // remove empty lines
        val tempOutput = StringBuilder()
        tempOutput.append(output)
        output.clear()
        output.append(tempOutput.replace(Regex.fromLiteral("(?m)^\\s+\$"),""))

        outputMeta.append("$numDir ${if (numDir > 1) "directories" else "directory"}, $numFile ${if (numFile > 1) "files" else "file"}, " +
                "$numSymLink ${if (numSymLink > 1) "symlinks" else "symlink"}")
    }

    private fun fileWalk(path: Path, space: String, prefix: String, vararg options: TreeOptions) {
        var walkHidden = false
        var walkSymLink = false
        val toPrint = StringBuilder()

        options.forEach {
            if (it == TreeOptions.WALK_HIDDEN) {
                walkHidden = true
            }
            if (it == TreeOptions.WALK_SYM_LINK) {
                walkSymLink = true
            }
        }

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
                output.append(toPrint)
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
            output.append(toPrint)

            var k = 0
            path.toFile().listFiles()?.sorted()?.forEach {
                if (++k == path.toFile().listFiles()?.size)
                    if (prefix == "├──")
                        fileWalk(it.toPath(), "$space│$SPACE", "└──", *options)
                    else
                        fileWalk(it.toPath(), "$space$SPACE", "└──", *options)
                else
                    if (prefix == "├──")
                        fileWalk(it.toPath(), "$space│$SPACE", "├──", *options)
                    else
                        fileWalk(it.toPath(), "$space$SPACE", "├──", *options)
            }
        } else {
            numFile++
            if (Files.probeContentType(path) == null)
                toPrint.append(
                    "${
                        if (space.startsWith(SPACE)) space.substring(SPACE.length)
                        else space
                    }$prefix $ANSI_GREEN${path.fileName}$ANSI_RESET"
                ).append("\n")
            else if (Files.probeContentType(path) == "image/jpeg")
                toPrint.append(
                    "${
                        if (space.startsWith(SPACE)) space.substring(SPACE.length)
                        else space
                    }$prefix $ANSI_PURPLE${path.fileName}$ANSI_RESET"
                ).append("\n")
            else if (Files.probeContentType(path) == "application/java-archive"
                || Files.probeContentType(path) == "application/gzip"
            )
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
                    }$prefix $ANSI_YELLOW${path.fileName}$ANSI_RESET"
                ).append("\n")
            output.append(toPrint)
        }
    }
}