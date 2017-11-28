package com.redhat.mqe

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

data class TestResult(val file: File, val suite: String)

val re = Regex(".*/(?<suite>[a-zAz-]*)/target/surefire-reports/(?<file>TEST-[a-zA-Z.]*\\.xml)", RegexOption.COMMENTS)

fun testResults(rootDirectory: Path): Sequence<TestResult> = rootDirectory.toFile().walkTopDown()
        .filter { it.isFile }
        .mapNotNull {
            val m = re.matchEntire(it.path)
            m?.run {
                TestResult(it, groups["suite"]!!.value)
            }
        }

fun compressTestResults(rootDirectory: Path, archiver: TarBz2Writer) {
    testResults(rootDirectory).forEach {
        val fileName = it.file.name
        archiver.putFile(it.file.toPath(), Paths.get(it.suite, fileName).toString())
    }
}