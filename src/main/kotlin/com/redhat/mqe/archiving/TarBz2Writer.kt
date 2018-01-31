package com.redhat.mqe.archiving

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import java.nio.file.Path

class TarBz2Writer(path: Path) : ArchiveWriter {
    private val fileOutput = path.toFile().outputStream()
    private val bz2Output = BZip2CompressorOutputStream(fileOutput)
    private val tarOutput = TarArchiveOutputStream(bz2Output)

    init {
        tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
    }

    override fun putFile(inputPath: Path, entryName: String) {
        val inputFile = inputPath.toFile()
        val entry = tarOutput.createArchiveEntry(inputFile, entryName)
        tarOutput.putArchiveEntry(entry)
        inputFile.inputStream().use { it.copyTo(tarOutput) }
        tarOutput.closeArchiveEntry()
    }

    override fun close() {
        tarOutput.close()
        bz2Output.close()
        fileOutput.close()
    }
}
