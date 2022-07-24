package mikufan.cx.executil

import java.nio.file.Path

/**
 * Start running a command line that is customized by a given [ProcessBuilder]
 * @param command Array<out String> command line
 * @param builder [@kotlin.ExtensionFunctionType] Function1<ProcessBuilder, Unit> process builder used to custom the process
 * @return Process a running process, which the [ProcessBuilder.start] method is already called
 */
inline fun runCmd(vararg command: String, builder: ProcessBuilder.() -> Unit = {}): Process =
  ProcessBuilder(*command).apply(builder).start()

/**
 * Start running a command line that is customized by a given [ProcessBuilder]
 * @param command Collection<out String> command line
 * @param builder [@kotlin.ExtensionFunctionType] Function1<ProcessBuilder, Unit> process builder used to custom the process
 * @return Process a running process, which the [ProcessBuilder.start] method is already called
 */
inline fun runCmd(command: Collection<out String>, builder: ProcessBuilder.() -> Unit = {}): Process =
  runCmd(*command.toTypedArray(), builder = builder)

var ProcessBuilder.directory: Path
  get() = this.directory().toPath()
  set(value) {
    this.directory(value.toFile())
  }

var ProcessBuilder.redirectOutput: ProcessBuilder.Redirect
  get() = this.redirectOutput()
  set(value) {
    this.redirectOutput(value)
  }

var ProcessBuilder.redirectError: ProcessBuilder.Redirect
  get() = this.redirectError()
  set(value) {
    this.redirectError(value)
  }

var ProcessBuilder.redirectErrorStream: Boolean
  get() = this.redirectErrorStream()
  set(value) {
    this.redirectErrorStream(value)
  }

var ProcessBuilder.redirectInput: ProcessBuilder.Redirect
  get() = this.redirectInput()
  set(value) {
    this.redirectInput(value)
  }

val ProcessBuilder.environment: MutableMap<String, String>
  get() = this.environment()

fun ProcessBuilder.addMoreEnvironment(newEnvironment: Map<String, String>) {
  environment.putAll(newEnvironment)
} 
