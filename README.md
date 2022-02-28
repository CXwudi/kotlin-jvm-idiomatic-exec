# Kotlin/JVM Idiomatic Exec
[![](https://jitpack.io/v/CXwudi/kotlin-jvm-idiomatic-exec.svg)](https://jitpack.io/#CXwudi/kotlin-jvm-idiomatic-exec)

Running an external process in with Kotlin/JVM has never been so easy 🤤

## Prerequisites

- Java 11+
- Kotlin (any recent version)

This library only supports Kotlin/JVM

## Features

- the idiomatic way to run an external process synchronously or asynchronously
- very lightweight, only 3 source files in this library

## How to import

Check [JitPack](https://jitpack.io/#CXwudi/kotlin-jvm-idiomatic-exec) to see how to import

## How to use

To run `java --version` as an external process:

```kotlin
runCmd("java", "--version").sync {
  onStdOutEachLine { logger.info(it) }
  onStdErrEachLine { logger.warn(it) }
}
// which is equivalent to 
runCmd("java", "--version").sync {
  onStdOut { this.forEachLine { logger.info(it) } }
  onStdErr { this.forEachLine { logger.warm(it) } }
}
```

This can be simplified as

```kotlin
runCmd("java", "--version").sync { withLogger(logger) }
// or with a different log level
runCmd("java", "--version").sync { withLogger(logger, stdOutLevel = Level.INFO, stdErrLevel = Level.WARN) }
```

You can also customize how process runs:

```kotlin
val myExecutor: ExecutorService = Executors.newFixedThreadPool(3) as ExecutorService

runCmd("ls") {
  directory = Path(".")
  redirectErrorStream = true
}.sync(timeout = 5, unit = TimeUnit.MINUTES, executor = myExecutor) {
  onStdOut(Charsets.UTF_8) { this.forEachLine { logger.info(it) } }
  // no need to handle std err as we have set redirectErrorStream = true
}
myExecutor.shutdown()
```

You can discard the std out and std err:

```kotlin
runCmd("my", "silent", "command").sync()
// which is equivalent to 
runCmd("my", "silent", "command").sync { silently() }
// which is equivalent to 
runCmd("my", "silent", "command").sync {
  onStdOut { }
  onStdErr { }
}
```

You can interact with the running process:

```kotlin
runCmd("cmd").sync {
  withInputs("python --version", "java --version", "gradle --version", "exit")
  withLogger(log.underlyingLogger, stdErrLevel = Level.WARN)
}
// which is equivalent to 
runCmd("cmd").sync {
  onStdIn { this writeLine "python --version" writeLine "java --version" writeLine "gradle --version" writeLine "exit" }
  onStdOutEachLine { logger.info(it) }
  onStdErrEachLine { logger.warn(it) }
}
```

Asynchronously? no problem:  
Simply change `sync()` to `async()` to turn any synchronized waiting to asynchronous `CompletableFuture`

```kotlin
val runningProcess: CompletableFuture<Process> = 
  runCmd("java", "--version").async { // sync -> async
    onStdOutEachLine { logger.info(it) }
    onStdErrEachLine { logger.warn(it) }
  }
```

For all others usages, refer to the only 3 [source files](lib/src/main/kotlin/mikufan/cx/executil/).
