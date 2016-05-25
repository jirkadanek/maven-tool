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
    val timeoutInSeconds = 10L * 60;

    val outputFile = File.createTempFile("output", null)
    val builder = ProcessBuilder()
    builder.directory(File(path))
    builder.command(listOf("mvn", "-P$profile", "-pl", ":$name", "verify"))
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

//fun <T> permutationsOf(list: List<T>, n Int): List<T> = when (n) {
//    0 -> emptyList()
//    else -> {
//        permutationsOf(, n - 1) + list.
//    }
//}

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

    when ("run") {
        "print" -> doPrintLExampleLeaves(pom)
        "jenkins" -> doPrintJenkinsExampleJobs(pom)
        "teamcity" -> doPrintTeamCityExampleJobs(pom)
        "run" -> doVerifyExampleLeaves(projectDirectory, outputDirectory, skip = listOf(
                //                "client-side-fileoverlistener", // timeouts and does not clean up
                "large-message" // runs too long
        ))
        else -> throw RuntimeException("Selected mode does not exist.")
    }
}