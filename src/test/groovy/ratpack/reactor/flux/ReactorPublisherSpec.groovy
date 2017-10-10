package ratpack.reactor.flux

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import ratpack.exec.Blocking
import ratpack.reactor.ReactorRatpack
import ratpack.test.exec.ExecHarness
import reactor.core.publisher.Flux
import spock.lang.AutoCleanup
import spock.lang.Specification

class ReactorPublisherSpec extends Specification {

  @AutoCleanup
  ExecHarness harness = ExecHarness.harness()
  AsyncService service = new AsyncService()

  static class AsyncService {
    public Flux<Void> fail() {
      ReactorRatpack.flux(Blocking.get { throw new RuntimeException("!!!") })
    }

    public <T> Flux<T> flux(T value) {
      ReactorRatpack.flux(Blocking.get { value })
    }
  }

  def setup() {
    ReactorRatpack.initialize()
  }

  def "convert RX Flux to ReactiveStreams Publisher"() {
    given:
    Publisher<String> pub = service.flux("foo").publisher()
    def received = []
    Subscription subscription
    boolean complete = false

    when:
    harness.run {
      pub.subscribe(new Subscriber<String>() {
        @Override
        void onSubscribe(Subscription s) {
          subscription = s
        }

        @Override
        void onNext(String s) {
          received << s
        }

        @Override
        void onError(Throwable t) {
          received << t
        }

        @Override
        void onComplete() {
          complete = true
        }
      })
      subscription.request(1)
    }

    then:
    received.size() == 1
    received.first() == 'foo'
    complete
  }

}
