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
    should("sync to logger") {
      // https://www.nicovideo.jp/watch/sm40348768
      // https://www.youtube.com/watch?v=IpKDXFGoLnE
      runCmd(
        "D:\\coding-workspace\\Vocaloid Coding POC\\Project VD Run Env\\yt-dlp.exe",
        "-v",
        "https://www.nicovideo.jp/watch/sm40348768",
        "--ffmpeg-location",
        "./ffmpeg.exe",
        "--write-thumbnail"
      ) {
        directory = Path.of("D:\\coding-workspace\\Vocaloid Coding POC\\Project VD Run Env\\")
//        redirectErrorStream()
      }.sync {
        onStdOutEachLine { log.info { it } }
        onStdErrEachLine { log.warn { it } }
      }
    }
  }

  context("run basic commands") {
    should("sync to logger") {
      runCmd("java", "--version").sync { withLogger(log.underlyingLogger) }
    }

    xshould("sync by manual functions") {
      runCmd("python", "--version").sync {
        onStdOutEachLine { log.info { it } }
        onStdErrEachLine { log.warn { it } }
      }
    }
  }

  context("run a interactive shell") {
    should("interact") {
      // for some reason, using bash won't print
      runCmd("bash").sync {
        withInputs("python --version", "java --version", "gradle --version", "exit")
        withLogger(log.underlyingLogger, stdErrLevel = Level.WARN)
      }
    }
    should("interact without easy syntax") {
      runCmd("bash").sync {
        onStdIn { this writeLine "python --version" writeLine "java --version" writeLine "gradle --version" writeLine "exit" }
        onStdOutEachLine { log.info { it } }
        onStdErrEachLine { log.warn { it } }
      }
    }
    should("successfully init a gradle project") {
      // for some reason, can't use gradle but gradle.bat
      // gradle init will not make interactive process
      runCmd("gradle", "init") {
        directory = Files.createTempDirectory(Path.of("."), "temp-gradle-").also { it.toFile().deleteOnExit() }
      }.sync {
        withInputs(
          "2", "4", "2", "2",
          "auto-gen-project", ""
        )
        withLogger(log.underlyingLogger, stdErrLevel = Level.WARN)
      }
    }
  }
})

private val log = KInlineLogging.logger()
