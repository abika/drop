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

    data class FileItem(val name: String, val size: Long, val lastModifiedTime: FileTime) {
        fun humanReadableSize(): String = when {
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

    }

    fun items(): List<FileItem> =
        fileDirectory
            .toAbsolutePath()
            .listDirectoryEntries()
            .map { path -> FileItem(path.name, path.fileSize(), path.getLastModifiedTime(LinkOption.NOFOLLOW_LINKS)) }
}
