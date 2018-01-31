package com.redhat.mqe

import com.google.common.truth.Truth.assertThat
import com.redhat.mqe.archiving.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

class Tests {
    private val root: Path = Paths.get(this.javaClass.classLoader.getResource("testProject").toURI())

    @Test
    fun matchingSurefireTestResultFiles() {
        assertThat("a/b/integration-tests/target/surefire-reports/TEST-org.apache.activemq.artemis.tests.integration.SingleServerSimpleTest.xml")
                .matches(re.toPattern())
    }

    @Test
    fun walkingResultsFileTree() {
        val results = testResults(root)
        assertThat(results.toList()).containsExactlyElementsIn(listOf(
                TestResult(
                        file = root.resolve("path/some-module/target/surefire-reports/TEST-some.test.xml").toFile(),
                        suite = "some-module")
        ))
    }

    @Test
    fun compressingTestResultsToZip() {
        val path = Paths.get("compressingTestResultsToZip.zip")
        try {
            val archiver = ZipWriter(path)
            compressTestResults(root, archiver)
            archiver.close()
        } finally {
            assertThat(path.toFile().delete()).isTrue()
        }
    }

    @Test
    fun compressingTestResultsToTarBz2() {
        val path = Paths.get("compressingTestResultsToTarBz2.tar.bz2")
        try {
            val archiver = TarBz2Writer(path)
            compressTestResults(root, archiver)
            archiver.close()
        } finally {
            assertThat(path.toFile().delete()).isTrue()
        }
    }

    @Test
    fun compressFilesToGivenPaths() {
        val path = Paths.get("compressFilesToGivenPaths.tar.bz2")
        try {
            val bz2 = TarBz2Writer(path)
            bz2.putFile(Paths.get("/etc/passwd"), "tests/surefire/mytest.xml")
            bz2.close()
        } finally {
            assertThat(path.toFile().delete()).isTrue()
        }
    }
}
