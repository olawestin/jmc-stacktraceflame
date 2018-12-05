# Java stacktrace flame graph #

A sample program using the Mission Control StacktraceModel to generate input data to Brendan Gregg's flamegraph program (<https://github.com/brendangregg/FlameGraph>).

## Build ###

Build using maven.

```bash
mvn package
```

## Running the program ##

The first argument is the input JFR file. The second optional argument is the output file (if not specified, it is printed on stdout).

```bash
java -jar stacktraceflame-0.0.1-SNAPSHOT.jar input.jfr > output.txt
java -jar stacktraceflame-0.0.1-SNAPSHOT.jar input.jfr output.txt
```

Then run the flamegraph renderer:

```bash
flamegraph.pl output.txt > output.svg
```

Or just pipe everything together:

```bash
java -jar stacktraceflame-0.0.1-SNAPSHOT.jar input.jfr | flamegraph.pl > output.svg
```
