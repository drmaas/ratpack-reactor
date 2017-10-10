package ratpack.reactor.internal;


import ratpack.exec.Execution;
import ratpack.exec.Promise;
import ratpack.func.Action;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.BiFunction;

public class ErrorHandler implements BiFunction<Throwable, Object, Throwable> {

  @Override
  public Throwable apply(Throwable throwable, Object o) {
    Throwable t;
    if (throwable instanceof UnsupportedOperationException) {
      t = throwable.getCause();
    } else if (throwable instanceof UndeclaredThrowableException) {
      t = ((UndeclaredThrowableException) throwable).getUndeclaredThrowable();
    } else {
      t = throwable;
    }
    if (Execution.isActive()) {
      Promise.error(t).then(Action.noop());
    }
    return t;
  }
}
