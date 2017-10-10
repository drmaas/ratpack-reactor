package ratpack.reactor.internal;

import reactor.core.scheduler.Scheduler;

public class DefaultSchedulers {

  private static Scheduler computationScheduler = new MultiExecControllerBackedScheduler();
  private static Scheduler ioScheduler = new MultiBlockingExecutorBackedScheduler();

  public static Scheduler getComputationScheduler() {
    return computationScheduler;
  }

  public static Scheduler getIoScheduler() {
    return ioScheduler;
  }

}
