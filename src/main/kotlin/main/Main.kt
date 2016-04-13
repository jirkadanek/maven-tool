package main

import maven.POM
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit


fun doPrintLExampleLeaves(pom: POM) {
    for (leaf in pom.leafArtifactIds("examples")) {
        println(leaf)
    }
}

fun doPrintJenkinsExampleJobs(pom: POM) {
    for (leaf in pom.leafArtifactIds("examples")) {
        print("""
      - upstream-artemis-examples:
          suffix: $leaf
          options: -Pexamples -pl :$leaf
        """)
    }
}

fun doVerifyExampleLeaves(path: String, output: String, skip: List<String>) {
    val profile = "examples"

    val pomFile = Paths.get(path).resolve("pom.xml")
    val pom = POM(pomFile.toString())
    for (projectName in pom.leafArtifactIds(profile).filterNot { it in skip }) {
        verifyExample(path, projectName, output)
    }
}

fun verifyExample(path: String, name: String, outputDir: String) {
    val profile = "examples"

    val outputFile = File.createTempFile("output", null)
    val builder = ProcessBuilder()
    builder.directory(File(path))
    builder.command("mvn", "-P$profile", "-pl", ":$name", "clean", "verify")
    builder.redirectOutput(outputFile)
    val process = builder.start()

    var succeeded = process.waitFor(120, TimeUnit.SECONDS)
    succeeded = succeeded && process.waitFor() == 0
    val group = if (succeeded) "succeeded" else "failed"

    // TODO(jdanek): check with ps that artemis (child process?) dies as well
    if (process.isAlive) {
        process.destroyForcibly()
        process.waitFor()
    }

    val dir = Paths.get(outputDir).resolve("$group")
    Files.createDirectories(dir)
    Files.move(outputFile.toPath(), Paths.get(outputDir).resolve("$group/$name.stdout"), StandardCopyOption.REPLACE_EXISTING)
}

fun main(args: Array<String>) {
    val projectDirectory: String
    val outputDirectory: String

    try {
        projectDirectory = args[0]
        outputDirectory = args[1]
    } catch (e: IndexOutOfBoundsException) {
        println("Usage: <projectDirectory> <outputDirectory>")
        return
    }

    val path = Paths.get(projectDirectory).resolve("pom.xml").toString()
    val pom = POM(path)

    when ("jenkins") {
        "print" -> doPrintLExampleLeaves(pom)
        "jenkins" -> doPrintJenkinsExampleJobs(pom)
        "run" -> doVerifyExampleLeaves(projectDirectory, outputDirectory, listOf("large-message"))
        else -> throw RuntimeException("Selected mode does not exist.")
    }
}