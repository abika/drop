package org.abika.model

import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.*

/**
 * @author Alexander Bikadorov {@literal <goto@openmailbox.org>}
 */
class FileModel(private val fileDirectory: Path) {
    enum class ItemType {
        DIRECTORY,
        FILE;
    }

    data class FileItem(val type: ItemType, val name: String, val size: Long, val lastModifiedTime: FileTime) : Comparable<FileItem> {
        fun humanReadableSize(): String = when {
            type == ItemType.DIRECTORY -> "<DIR>"
            size >= 1 shl 30 -> "%.1f GB".format(size.toDouble() / (1 shl 30))
            size >= 1 shl 20 -> "%.1f MB".format(size.toDouble() / (1 shl 20))
            size >= 1 shl 10 -> "%.0f kB".format(size.toDouble() / (1 shl 10))
            else -> "$size bytes"
        }

        fun humanReadableLastModifiedTime(): String =
            lastModifiedTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))

        override fun compareTo(other: FileItem): Int {
            return Comparator.comparing(FileItem::type)
                .thenComparing(FileItem::name)
                .compare(this, other)
        }

    }

    fun items(): List<FileItem> {
        fun itemType(path: Path): ItemType =
            if (path.isRegularFile())
                ItemType.FILE
            else
                ItemType.DIRECTORY

        return fileDirectory
            .toAbsolutePath()
            .listDirectoryEntries()
            .map { path -> FileItem(itemType(path), path.name, path.fileSize(), path.getLastModifiedTime(LinkOption.NOFOLLOW_LINKS)) }
            .sortedBy { item -> item }
    }
}
