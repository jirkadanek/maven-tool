package com.redhat.mqe.archiving

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.nio.file.Path

class ZipWriter(path: Path) : ArchiveWriter {
    private val fileOutput = path.toFile().outputStream()
    private val zipOutput = ZipArchiveOutputStream(fileOutput)

    override fun putFile(inputPath: Path, entryName: String) {
        val inputFile = inputPath.toFile()
        val entry = zipOutput.createArchiveEntry(inputFile, entryName)
        zipOutput.putArchiveEntry(entry)
        inputFile.inputStream().use { it.copyTo(zipOutput) }
        zipOutput.closeArchiveEntry()
    }

    override fun close() {
        zipOutput.close()
        fileOutput.close()
    }
}
