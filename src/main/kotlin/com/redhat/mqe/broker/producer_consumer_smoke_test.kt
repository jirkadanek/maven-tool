//package com.redhat.mqe
//
//class RunTest {
//    fun runProducerConsumerSmokeTest() {
//        val env = mapOf("JAVA_HOME" to "/qa/home/jdanek/hpux/java8/")
//        val pb = ProcessBuilder()
//        pb.command(listOf(""))
//        pb.environment().putAll(env)
//        val p = pb.start()
//    }
//
//    fun test() {
//        fetch()
//        unzip()
//        val t = startBroker()
//
//    }
//}
//
//
//// monitor either broker log or stdout for events, register listener -- broker finished startup, monitor ports, ...
//
//export JAVA_HOME=/qa/home/jdanek/hpux/java8/
//#export JAVA_ARGS=-Dio.netty.noUnsafe=true
//
//env
//java -version
//${ JAVA_HOME }/bin/java -version
//wget --no-check-certificate ${ amq7_download_url }
//unzip `basename ${amq7_download_url}`
//bdir=`find . -name "amq-broker-*" -type d`
//${ bdir }/bin/artemis create --user admin --password admin --role admin --allow-anonymous i0
//i0/bin/artemis run &
//pid=$!
//sleep 10  # give broker time to start up
//i0/bin/artemis producer
//i0/bin/artemis consumer
//kill ${ pid }
