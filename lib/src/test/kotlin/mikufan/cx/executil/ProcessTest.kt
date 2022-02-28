package mikufan.cx.executil

import io.kotest.core.spec.style.ShouldSpec
import mikufan.cx.inlinelogging.KInlineLogging
import org.slf4j.event.Level
import java.nio.file.Files
import java.nio.file.Path

/**
 * @date 2021-07-30
 * @author CX无敌
 */
class ProcessTest : ShouldSpec({

  context("run youtube-dl commands") {
    xshould("sync to logger") {
      val cmd = "python .\\youtube_dl\\__main__.py -v https://www.youtube.com/watch?v=PHW87EW6KRk --ffmpeg-location ..\\..\\ffmpeg.exe"
      runCmd(*cmd.split(' ').toTypedArray()) {
        directory = Path.of("D:\\11134\\Videos\\Vocaloid Coding POC\\Video Downloader POC\\official-youtube-dl")
      }.sync { withLogger(log.underlyingLogger) }
    }
  }

  context("run basic commands") {
    should("sync to logger") {
      runCmd("java", "--version").sync { withLogger(log.underlyingLogger) }
    }

    should("sync by manual functions") {
      runCmd("python", "--version").sync {
        onStdOutEachLine { log.info { it } }
        onStdErrEachLine { log.warn { it } }
      }
    }
  }
  
  context("run a interactive shell") {
    should("interact") {
      // for some reason, using bash won't print
      runCmd("cmd").sync {
        withInputs("python --version", "java --version", "gradle --version", "exit")
        withLogger(log.underlyingLogger, stdErrLevel = Level.WARN)
      }
    }
    should("interact without easy syntax") {
      runCmd("cmd").sync {
        onStdIn { this writeLine "python --version" writeLine "java --version" writeLine "gradle --version" writeLine "exit" }
        onStdOutEachLine { log.info { it } }
        onStdErrEachLine { log.warn { it } }
      }
    }
    should("successfully init a gradle project") {
      // for some reason, can't use gradle but gradle.bat
      // gradle init will not make interactive process
      runCmd("gradle.bat", "init") {
        directory = Files.createTempDirectory(Path.of("."), "temp-gradle-").also { it.toFile().deleteOnExit() }
      }.sync {
        withInputs("2", "4", "2", "2",
        "auto-gen-project", "")
        withLogger(log.underlyingLogger, stdErrLevel = Level.WARN)
      }
    }
  }
})

private val log = KInlineLogging.logger()
