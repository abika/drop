package org.abika.model

import java.nio.file.Path
import kotlin.io.path.*
/**
 * @author Alexander Bikadorov {@literal <goto@openmailbox.org>}
 */
class FileModel(private val fileDirectory: Path) {

    fun items(): List<FileItem> {
        val path = fileDirectory.toAbsolutePath()

        println("path=$path")

        return path.listDirectoryEntries().map { path -> FileItem(path.name, path.fileSize()) }
    }

    data class FileItem(val name: String, val size: Long) {
        fun humanReadableSize() = when {
            size >= 1 shl 30 -> "%.1f GB".format(size.toDouble() / (1 shl 30))
            size >= 1 shl 20 -> "%.1f MB".format(size.toDouble() / (1 shl 20))
            size >= 1 shl 10 -> "%.0f kB".format(size.toDouble() / (1 shl 10))
            else -> "$size bytes"
        }
    }
}
