# Java Static Analyses

A collection of intra-procedural static analyses for Java programs implemented using Soot

## Instructions
The project is built using `maven`.

```
cd java-static-analyses

mvn clean package
```

This creates a seperate `jar` file for each of the analyses. To run:

```
java -jar target/[analysis]-jar-with-dependencies.jar examples/ Example1 main
```

Here, the first argument is the directory where the examples are (classpath), the target class and the target function.

For example:

```
java -jar target/live_variables-jar-with-dependencies.jar examples/ Example1 main
```
