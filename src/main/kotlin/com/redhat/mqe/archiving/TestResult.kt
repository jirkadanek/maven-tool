package com.redhat.mqe.archiving

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

data class TestResult(val file: File, val suite: String)

val re = Regex(".*/(?<suite>[^/]*)/target/surefire-reports/(?<file>TEST-[^/]*\\.xml)", RegexOption.COMMENTS)

interface ArchiveWriter {
    fun putFile(inputPath: Path, entryName: String)
    fun close()
}

fun testResults(rootDirectory: Path): Sequence<TestResult> = rootDirectory.toFile().walkTopDown()
        .filter { it.isFile }
        .mapNotNull {
            val m = re.matchEntire(it.path)
            m?.run {
                TestResult(it, groups["suite"]!!.value)
            }
        }

fun compressTestResults(rootDirectory: Path, archiver: ArchiveWriter) {
    testResults(rootDirectory).forEach {
        val fileName = it.file.name
        archiver.putFile(it.file.toPath(), Paths.get(it.suite, fileName).toString())
    }
}

fun compressTestResultsIntoSureFireReports(rootDirectory: Path, archiver: ArchiveWriter) {
    val suite = "surefire-reports"
    testResults(rootDirectory).forEach {
        val fileName = it.file.name
        archiver.putFile(it.file.toPath(), Paths.get(suite, fileName).toString())
    }
}