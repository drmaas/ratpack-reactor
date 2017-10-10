package ratpack.reactor.flux

import ratpack.http.client.HttpClient
import ratpack.reactor.ReactorRatpack
import ratpack.reactor.internal.BaseHttpClientSpec

import java.util.function.Function

import static ReactorRatpack.flux

class ReactorHttpClientSpec extends BaseHttpClientSpec {

  def setup() {
    ReactorRatpack.initialize()
  }

  def "can use rx with http client"() {
    given:
    otherApp {
      get("foo") { render "bar" }
    }

    when:
    handlers {
      get { HttpClient httpClient ->
        flux(httpClient.get(otherAppUrl("foo")) {}).map({
          it.body.text.toUpperCase()
        } as Function) subscribe {
          render it
        }
      }
    }

    then:
    text == "BAR"
  }

}
