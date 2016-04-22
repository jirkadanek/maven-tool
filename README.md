# maven-tool

Java program to query maven projects and to selectively build some submodules. Made for building Artemis examples.

## Artemis examples (design document)

Artemis examples can be built independently, but running a reactor with -Pexamples fails. Running with --fail-at-end tends to fail spectacularly (servers are not being stopped).

I need to be able to run each example individually, then try all pairs, or a random sample of all pairs. This should provide interpretable results to fill effective bug reports.

### Implemented Features

  * discover an print a list of examples
  * run each example individually

## Building and Running

    mvn package
    java -cp <path/to/.jar> main.MainKt <path/to/activemq-artemis/examples> </path/to/output/dir>

## Dependencies

Apache Maven, `mvn`, is required to build the project and also must be present in `$PATH` in order for it to run.