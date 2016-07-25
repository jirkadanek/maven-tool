# maven-tool

JVM program to query maven projects and to selectively build some submodules. Made for building and running Apache Artemis examples.

## Artemis examples (design document)

Artemis examples can be built independently, but running a reactor with `-Pexamples` fails. Running with `--fail-at-end` tends to fail spectacularly (servers are not being stopped).

I need to be able to run each example individually, then try all pairs, or a random sample of all pairs. This should provide interpretable results to fill effective bug reports.

### Implemented Features

  * discover and print a list of examples
  * run (i.e. `mvn verify`) each example individually

## Configuration

Rewrite a string at the end of Main.kt. TODO: make it into a command line option.

## Building and Running

First, download A-MQ and Maven repo. Create `~/.m2/settings.xml` for Maven, see ENTMQ-1591.

    wget http://download.eng.bos.redhat.com/brewroot/packages/org.rh-messaging.AMQ7-A-MQ7-parent/7.0.0.ER6_redhat_1/1/maven/org/rh-messaging/AMQ7/A-MQ7/7.0.0.ER6-redhat-1/A-MQ7-7.0.0.ER6-redhat-1-bin.zip
    unzip A-MQ7-7.0.0.ER6-redhat-1-bin.zip
    wget http://download.eng.bos.redhat.com/devel/candidates/amq/AMQ-BROKER-7.0.0.ER6/jboss-amq7-7.0.0.ER6-maven-repository.zip
    unzip jboss-amq7-7.0.0.ER6-maven-repository.zip
    vim ~/.m2/settings.xml

Do `mvn -Pexamples dependency:resolve` in A-MQ examples directory, or possibly even `compile` or `package`, because the first command does not download everything. That other command does not download everything either, but it's enough. This is necessary because maven-tool would timeout long running examples and fetching dependencies and compiling for the first time takes long.

    mvn package
    java -cp <path/to/.jar> main.MainKt <path/to/activemq-artemis/examples> </path/to/output/dir>

## Dependencies

Apache Maven, `mvn`, is required to build the project and also must be present in `$PATH` in order for it to run.