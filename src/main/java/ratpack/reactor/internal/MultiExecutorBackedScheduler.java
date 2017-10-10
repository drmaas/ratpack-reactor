package ratpack.reactor.internal;

import com.google.common.collect.MapMaker;
import ratpack.exec.ExecController;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class MultiExecutorBackedScheduler implements Scheduler {
  private final ConcurrentMap<ExecController, Scheduler> map = new MapMaker().weakKeys().weakValues().makeMap();
  private final AtomicReference<Scheduler> fallback = new AtomicReference<>();

  public abstract ExecutorBackedScheduler getExecutorBackedScheduler(ExecController execController);

  private Scheduler getDelegateScheduler() {
    return ExecController.current()
      .map(c -> map.computeIfAbsent(c, this::getExecutorBackedScheduler))
      .orElseGet(() -> {
        if (fallback.get() == null) {
          int nThreads = Runtime.getRuntime().availableProcessors();
          ExecutorService executor = Executors.newFixedThreadPool(nThreads);
          Scheduler scheduler = Schedulers.fromExecutor(executor);
          fallback.compareAndSet(null, scheduler);
        }
        return fallback.get();
      });
  }

  @Override
  public Disposable schedule(Runnable runnable) {
    return getDelegateScheduler().schedule(runnable);
  }

  @Override
  public Scheduler.Worker createWorker() {
    return getDelegateScheduler().createWorker();
  }

  @Override
  public long now(@NonNull TimeUnit unit) {
    return getDelegateScheduler().now(unit);
  }
}
