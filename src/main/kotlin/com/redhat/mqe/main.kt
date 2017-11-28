package com.redhat.mqe

import java.nio.file.Paths

fun main(args: Array<String>) {
    when (args[0]) {
        "runTests" -> runTests()
        "compressJUnit" -> compressJUnit()
        else -> throw IllegalArgumentException()
    }
}

// reduce the output size
//  compress right away
//  print only failing outputs
// monitor
//  leaked treads
//  memory usage for every test, could be better done in JUnit itself through rules, maybe
//  i am closing...
fun runTests() {
    val r = TestRunCommands()
    val executor = Executor(r)
    r.listeners.add(Printer())
//    r.listeners.add(executor)
    r.run()
    if (executor.failed) {
        throw RuntimeException("Some execution failed")
    }

}

fun compressJUnit() {
    val archiver = TarBz2Writer(Paths.get("tests.tar.bz2"))
    compressTestResults(Paths.get("."), archiver)
    archiver.close()
}
