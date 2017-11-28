package com.redhat.mqe

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import java.nio.file.Path

class TarBz2Writer(val path: Path) {
    val fileOutput = path.toFile().outputStream()
    val bz2Output = BZip2CompressorOutputStream(fileOutput)
    val tarOutput = TarArchiveOutputStream(bz2Output)

    fun putFile(inputPath: Path, entryName: String) {
        val inputFile = inputPath.toFile()
        val entry = tarOutput.createArchiveEntry(inputFile, entryName)
        tarOutput.putArchiveEntry(entry)
        inputFile.inputStream().use { it.copyTo(tarOutput) }
        tarOutput.closeArchiveEntry()
    }

    fun close() {
        tarOutput.close()
        bz2Output.close()
        fileOutput.close()
    }
}
