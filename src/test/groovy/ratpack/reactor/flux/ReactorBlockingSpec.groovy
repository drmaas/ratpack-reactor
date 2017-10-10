package ratpack.reactor.flux

import ratpack.error.ServerErrorHandler
import ratpack.exec.Blocking
import ratpack.reactor.ReactorRatpack
import ratpack.reactor.internal.RatpackGroovyDslSpec
import ratpack.reactor.internal.SimpleErrorHandler

import java.util.function.Consumer
import java.util.function.Function

import static ReactorRatpack.flux
import static ratpack.reactor.ReactorRatpack.fluxEach

class ReactorBlockingSpec extends RatpackGroovyDslSpec {

  def setup() {
    ReactorRatpack.initialize()
  }

  def "can observe the blocking"() {
    when:
    handlers {
      get(":value") {
        flux(Blocking.get {
          pathTokens.value
        }) map({
          it * 2
        } as Function) map({
          it.toString().toUpperCase()
        } as Function) subscribe {
          render it
        }
      }
    }

    then:
    getText("a") == "AA"
  }

  def "blocking errors are sent to the context renderer"() {
    when:
    bindings {
      bind ServerErrorHandler, SimpleErrorHandler
    }
    handlers {
      get(":value") {
        flux(Blocking.get {
          pathTokens.value
        }) map({
          it * 2
        } as Function) map({
          throw new Exception("!!!!")
        } as Function) subscribe{
          render "shouldn't happen"
        }
      }
    }

    then:
    getText("a").startsWith new Exception("!!!!").toString()
    response.statusCode == 500
  }

  def "blocking errors can be caught by onerror"() {
    when:
    bindings {
      bind ServerErrorHandler, SimpleErrorHandler
    }
    handlers {
      get(":value") {
        flux(Blocking.get {
          pathTokens.value
        }) map({
          it * 2
        } as Function) map({
          throw new Exception("!!!!")
        } as Function) subscribe({
          render "shouldn't happen"
        }, { render "b" })
      }
    }

    then:
    getText("a") == "b"
  }

  def "can observe the blocking operation with an Iterable return type"() {
    when:
    handlers {
      get(":value") {
        def returnString = ""

        fluxEach(Blocking.get {
          pathTokens.value.split(",") as List
        })
          .take(2)
          .map({ it.toLowerCase() } as Function)
          .subscribe({
          returnString += it
        } as Consumer, { Throwable error ->
          throw error
        } as Consumer, {
          render returnString
        } as Runnable)
      }
    }

    then:
    getText("A,B,C") == "ab"
  }
}

