@file:Suppress("NOTHING_TO_INLINE")
package mikufan.cx.executil

import org.slf4j.Logger
import org.slf4j.event.Level
import java.io.BufferedWriter
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService

/**
 * @date 2021-07-17
 * @author CX无敌
 */
class ProcessSyncer(
  val executor: ExecutorService,
  val process: Process
) {

  inline fun onStdOut(charset: Charset = Charsets.UTF_8, crossinline handler: (String) -> Unit) {
    process.inputStream?.let {
      executor.execute {
        it.bufferedReader(charset).useLines { it.forEach(handler) }
      }
    }
  }

  inline fun onStdErr(charset: Charset = Charsets.UTF_8, crossinline handler: (String) -> Unit) {
    process.errorStream?.let {
      executor.execute {
        it.bufferedReader(charset).useLines { it.forEach(handler) }
      }
    }
  }

  inline fun onStdIn(charset: Charset = Charsets.UTF_8, crossinline handler: BufferedWriter.() -> Unit) {
    process.outputStream?.let {
      executor.execute {
        it.bufferedWriter(charset).use { it.handler() }
      }
    }
  }

  /**
   * convenience function to flush and discard the std out and std err stream.
   *
   * useful when handling std out and std err is not needed,
   * but don't want the process hangs because of not flushing streams
   */
  inline fun silently() {
    onStdOut { }
    onStdErr { }
  }

  /**
   * convenience function to redirect std out and std err to slf4j logger
   *
   * std out -> info
   *
   * std err -> debug
   */
  inline fun withLogger(logger: Logger, stdOutLevel: Level = Level.INFO, stdErrLevel: Level = Level.DEBUG) {
    val logStdOut = chooseLogMethod(stdOutLevel)
    onStdOut {
      logStdOut(it, logger)
    }
    val logStdErr = chooseLogMethod(stdErrLevel)
    onStdErr {
      logStdErr(it, logger)
    }
  }

  /**
   * convenience function to write each string into std in, using [Charsets.UTF_8]
   */
  inline fun withInputs(vararg lines: String) {
    onStdIn { lines.forEach { this writeLine it } }
  }

  /**
   * convenience function to write each string into std in, using a custom [Charsets]
   */
  inline fun withInputs(charset: Charset, vararg lines: String) {
    onStdIn(charset) { lines.forEach { this writeLine it } }
  }

  fun chooseLogMethod(level: Level): (String, Logger) -> Unit = when (level) {
    Level.TRACE -> { str, logger -> logger.trace(str) }
    Level.DEBUG -> { str, logger -> logger.debug(str) }
    Level.INFO -> { str, logger -> logger.info(str) }
    Level.WARN -> { str, logger -> logger.warn(str) }
    Level.ERROR -> { str, logger -> logger.error(str) }
  }
}

inline infix fun BufferedWriter.write(string: String) = this.apply { write(string) }
inline infix fun BufferedWriter.writeLine(string: String) = this.apply { write(string); newLine() }
