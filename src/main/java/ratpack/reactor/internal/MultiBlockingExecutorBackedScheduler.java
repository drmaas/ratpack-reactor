package ratpack.reactor.internal;

import ratpack.exec.ExecController;

public class MultiBlockingExecutorBackedScheduler extends MultiExecutorBackedScheduler {

  @Override
  public ExecutorBackedScheduler getExecutorBackedScheduler(ExecController execController) {
    return new BlockingExecutorBackedScheduler(execController);
  }

}
