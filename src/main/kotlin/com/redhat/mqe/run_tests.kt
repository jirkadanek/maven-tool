package com.redhat.mqe

import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


val version = "2.3.0.amq-710003-redhat-1"
val classifier = "tests"

interface Commander {
    val CWD: Path
    fun cd(relative: String)
    fun exec(command: List<String>)
}

class Printer : Commander {
    override val CWD: Path
        get() = TODO("not implemented")

    override fun cd(relative: String) {
        println("cd $relative")
    }

    override fun exec(command: List<String>) {
        println(command.joinToString(separator = " "))
    }
}

class Executor(val c: Commander) : Commander {
    override val CWD
        get() = c.CWD
    var failed = false

    override fun cd(relative: String) = Unit

    override fun exec(command: List<String>) {
        try {
            val p = ProcessBuilder()
                    .command(command)
                    .directory(CWD.toFile())
                    .start()
            p.waitFor()
            if (p.exitValue() != 0) {
                failed = true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            failed = true
        }
    }
}

class TestRunCommands : Commander {
    val listeners = mutableListOf<Commander>()
    override var CWD = Paths.get(".")

    fun cd(relative: String, f: () -> Unit) {
        val oldcwd = CWD
        try {
            cd(relative)
            f()
        } finally {
            cd(CWD.relativize(oldcwd).toString())
        }
    }

    fun exec(command: String) {
        exec(command.split(" "))
    }

    override fun cd(relative: String) {
        CWD = CWD.resolve(relative).normalize()
        listeners.forEach { it.cd(relative) }
    }

    override fun exec(command: List<String>) {
        listeners.forEach { it.exec(command) }
    }

    fun run() {
        for (testDependency in listOf("artemis-core-client", "artemis-commons", "artemis-server")) {
            cd(testDependency) {
                exec("mvn -U -s ../settings.xml test-compile")
                exec("mvn -U -s ../settings.xml jar:test-jar")
                exec("mvn -U -s ../settings.xml install:install-file -Dfile=target/$testDependency-$version-$classifier.jar -DpomFile=pom.xml -Dclassifier=$classifier")
            }
        }
        cd("tests") {
            // this is likely not necessary in, ... well, -am? does not help
            for (testDependency in listOf("unit-tests", "integration-tests")) {
                cd(testDependency) {
                    exec("mvn -U -s ../../settings.xml test-compile")
                    exec("mvn -U -s ../../settings.xml jar:test-jar")
                    exec("mvn -U -s ../../settings.xml install:install-file -Dfile=target/$testDependency-$version-$classifier.jar -DpomFile=pom.xml -Dclassifier=$classifier")
                }
            }
            exec("mvn -U -s ../settings.xml -Pjacoco -Ptests -Pextra-tests -Popenwire-tests test -Drat.ignoreErrors=true -Djasypt-version=1.9.3.redhat_3 -Dactivemq5.project.version=5.14.0")
        }
    }
}
