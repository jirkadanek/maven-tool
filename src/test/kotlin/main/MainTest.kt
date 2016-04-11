package main

import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class MainTest {
    private val module1 = "selfTestData/project/module1"
    @Test fun verifyExample() {
        var tmpdir: Path? = null
        try {
            tmpdir = Files.createTempDirectory(null)
            main.verifyExample(module1, "thisModuleNotExists", tmpdir.toString())
            assert(Files.exists(tmpdir.resolve("failed/thisModuleNotExists.stdout")))
        } finally {
            Runtime.getRuntime().exec("rm -r '${tmpdir.toString()}'")
        }
    }
}