package ratpack.reactor.internal;

import ratpack.exec.ExecController;

import java.util.concurrent.ExecutorService;

public class BlockingExecutorBackedScheduler extends ExecutorBackedScheduler {

  public BlockingExecutorBackedScheduler(ExecController execController) {
    super(execController);
  }

  @Override
  ExecutorService getExecutor() {
    return execController.getBlockingExecutor();
  }
}
