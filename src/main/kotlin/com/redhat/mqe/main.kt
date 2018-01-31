package com.redhat.mqe

import com.redhat.mqe.archiving.TarBz2Writer
import com.redhat.mqe.archiving.ZipWriter
import com.redhat.mqe.archiving.compressTestResults
import com.redhat.mqe.broker.Executor
import com.redhat.mqe.broker.Printer
import com.redhat.mqe.broker.TestRunCommands
import java.nio.file.Paths

fun main(args: Array<String>) {
    when (args[0]) {
        "runBrokerSystemTests" -> runBrokerSystemTests()
        "compressJUnit" -> compressJUnit(args.slice(1..args.size))
        else -> throw IllegalArgumentException("Specified operation is not implemented")
    }
}

// 6 hours for integration tests?!?
// aio/nio, install the lib? I guess so
//  there is a test to check if aio can be loaded that fails if libaio is not installed
// reduce the output size
//  compress right away
//  print only failing outputs
// monitor
//  leaked treads
//  memory usage for every test, could be better done in JUnit itself through rules, maybe
//  i am closing... messages
fun runBrokerSystemTests() {
    val r = TestRunCommands()
    val executor = Executor(r)
    r.listeners.add(Printer())
//    r.listeners.add(executor)
    r.run()
    if (executor.failed) {
        throw RuntimeException("Some execution failed")
    }

}

fun compressJUnit(args: List<String>) {
    val resultsDirectory = Paths.get(args[0])
    val archivePath = Paths.get(args[1])
    val archiver = when {
        archivePath.endsWith(".xip") -> ZipWriter(archivePath)
        archivePath.endsWith(".tar.bz2") -> TarBz2Writer(archivePath)
        else -> throw IllegalArgumentException("Only .zip and .tar.bz2 archive file extensions are supported")
    }
    compressTestResults(resultsDirectory, archiver)
    archiver.close()
}
