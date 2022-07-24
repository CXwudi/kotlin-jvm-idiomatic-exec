package mikufan.cx.executil

import java.util.concurrent.*

/**
 * Wait and sync on a running process, using a temporary [ThreadPoolExecutor]
 * @receiver Process the running process to wait on
 * @param timeout Long the timeout
 * @param unit TimeUnit the timeout unit
 * @param setupHandler [@kotlin.ExtensionFunctionType] Function1<ProcessSyncer, Unit> the setup handler of the [ProcessSyncer]
 * @return Process this process
 */
fun Process.sync(
  timeout: Long = 10,
  unit: TimeUnit = TimeUnit.MINUTES,
  setupHandler: ProcessSyncer.() -> Unit = { }
): Process {
  val executor: ExecutorService = ThreadPoolExecutor(
    3,
    3,
    1,
    TimeUnit.MINUTES,
    LinkedBlockingQueue(3),
    Executors.defaultThreadFactory()
  )
  val syncer = ProcessSyncer(executor, this)
  syncer.silently()
  syncer.setupHandler()
  syncer.startSync()
  executor.shutdown()
  waitFor(timeout, unit)
  // waiting for syncer threads, but should not be waiting too long as syncer threads should end shortly after the process ends
  syncer.waitForSync(1, TimeUnit.MINUTES)
  // finally, wait for termination, shouldn't be too long either
  executor.awaitTermination(1, TimeUnit.MINUTES)
  return this
}

/**
 * Wait and sync on a running process, using an existing [ThreadPoolExecutor]
 * @receiver Process the running process to wait on
 * @param timeout Long the timeout
 * @param unit TimeUnit the timeout unit
 * @param executor ExecutorService the executor that user provides to use
 * @param setupHandler [@kotlin.ExtensionFunctionType] Function1<ProcessSyncer, Unit> the setup handler of the [ProcessSyncer]
 * @return Process this process
 */
fun Process.sync(
  timeout: Long = 10,
  unit: TimeUnit = TimeUnit.MINUTES,
  executor: ExecutorService,
  setupHandler: ProcessSyncer.() -> Unit = { }
): Process {
  val syncer = ProcessSyncer(executor, this)
  syncer.silently()
  syncer.setupHandler()
  syncer.startSync()
  waitFor(timeout, unit)
  // waiting for syncer threads, but should not be waiting too long as syncer threads should end shortly after the process ends
  syncer.waitForSync(1, TimeUnit.MINUTES)
  // do not await termination on external thread pools. users should shut down the executor themselves.
  return this
}

// this requires Java 11
/**
 * Asynchronously sync on a running process, using a temporary [ThreadPoolExecutor]
 */
fun Process.async(
  setupHandler: ProcessSyncer.() -> Unit = { }
): CompletableFuture<Process> {
  val executor: ExecutorService = ThreadPoolExecutor(
    3,
    3,
    1,
    TimeUnit.MINUTES,
    LinkedBlockingQueue(3),
    Executors.defaultThreadFactory()
  )
  val syncer = ProcessSyncer(executor, this)
  syncer.silently()
  syncer.setupHandler()
  syncer.startSync()
  return onExit().completeAsync({
    executor.shutdown()
    // waiting for syncer threads, but should not be waiting too long as syncer threads should end shortly after the process ends
    syncer.waitForSync(1, TimeUnit.MINUTES)
    // finally, wait for termination, shouldn't be too long either
    executor.awaitTermination(1, TimeUnit.MINUTES)
    this
  }, executor)
}

/**
 * Asynchronously sync on a running process, using an existing [ThreadPoolExecutor]
 */
fun Process.async(
  executor: ExecutorService,
  setupHandler: ProcessSyncer.() -> Unit = { }
): CompletableFuture<Process> {
  val syncer = ProcessSyncer(executor, this)
  syncer.silently()
  syncer.setupHandler()
  syncer.startSync()
  return onExit().completeAsync({
    // waiting for syncer threads, but should not be waiting too long as syncer threads should end shortly after the process ends
    syncer.waitForSync(1, TimeUnit.MINUTES)
    // do not await termination on external thread pools. users should shut down the executor themselves.
    this
  }, executor)
}
