# maven-tool

Java program to query maven projects and to selectively build some submodules. Made for building Artemis examples.

## Artemis examples (design document)

Artemis examples can be built independently, but running a reactor with -Pexamples fails. Running with --fail-at-end tends to fail spectacularly (servers are not being stopped).

I need to be able to run each example individually, then try all pairs, or a random sample of all pairs. This should provide interpretable results to fill effective bug reports.