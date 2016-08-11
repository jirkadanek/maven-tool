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
        println("""            - $leaf""")
    }
}

fun doPrintTeamCityExampleJobs(pom: POM) {
    print(pom.leafArtifactIds("examples").joinToString(separator = ","))
}

fun doVerifyExampleLeaves(path: String, output: String, skip: List<String>) {
    val profile = "examples"

    val pomFile = Paths.get(path).resolve("pom.xml")
    val pom = POM(pomFile.toString())
    for (projectName in pom.leafArtifactIds(profile).filterNot { it in skip }) {
        verifyExample(path, projectName, output)
    }
}

private fun killallArtemis() {
    val builder = ProcessBuilder(listOf("pkill", "-SIGKILL", "-f", "org.apache.activemq.artemis.boot.Artemis"))
    builder.start().waitFor()
}

fun verifyExample(path: String, name: String, outputDir: String) {
    val profile = "examples"
    val minute = 60
    val timeoutInSeconds = 20L * minute

    val outputFile = File.createTempFile("output", null)
    val builder = ProcessBuilder()
    builder.directory(File(path))
    builder.command(listOf("mvn", "-debug", "-P$profile", "-pl", ":$name", "verify"))
    builder.redirectOutput(outputFile)
    val process = builder.start()

    var succeeded = process.waitFor(timeoutInSeconds, TimeUnit.SECONDS)
    succeeded = succeeded && process.waitFor() == 0
    val group = if (succeeded) "succeeded" else "failed"

    if (process.isAlive) {
        process.destroyForcibly().waitFor()
    }
    killallArtemis()

    val dir = Paths.get(outputDir).resolve("$group")
    Files.createDirectories(dir)
    Files.move(outputFile.toPath(), Paths.get(outputDir).resolve("$group/$name.stdout"), StandardCopyOption.REPLACE_EXISTING)
}

fun main(args: Array<String>) {
    val subcommand: String
    val projectDirectory: String

    try {
        subcommand = args[0]
        projectDirectory = args[1]
    } catch (e: IndexOutOfBoundsException) {
        println("Usage: <subcommand> <projectDirectory> [...]")
        return
    }

    val path = Paths.get(projectDirectory).resolve("pom.xml").toString()
    val pom = POM(path)

    val subcommands = hashMapOf(
            Pair("print", { doPrintLExampleLeaves(pom) }),
            Pair("jenkins", { doPrintJenkinsExampleJobs(pom) }),
            Pair("teamcity", { doPrintTeamCityExampleJobs(pom) }),
            Pair("run", fun() {
                val outputDirectory: String
                try {
                    outputDirectory = args[2]
                } catch (e: IndexOutOfBoundsException) {
                    println("Usage: run <projectDirectory> <outputDirectory>")
                    return
                }
                doVerifyExampleLeaves(projectDirectory, outputDirectory, skip = listOf(
                        "large-message" // runs too long
                ))
            })
    )

    if (!subcommands.containsKey(subcommand)) {
        println("Subcommands: ${subcommands.keys.joinToString()}")
        throw RuntimeException("Selected command '$subcommand' does not exist.")
    } else {
        subcommands[subcommand]?.invoke()
    }
}
