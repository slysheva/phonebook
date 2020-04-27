package simulation

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps


class GatlingSpec extends Simulation {

  val httpConf: HttpProtocolBuilder = http.baseUrl("http://localhost:9000/")

  val indexReq: ChainBuilder = repeat(500) {
    exec(
      http("Index").get("/").check(status.is(200))
    )
  }

  val readClientsScenario: ScenarioBuilder = scenario("PhoneBook").exec(indexReq).pause(1)

  setUp(
    readClientsScenario.inject(rampUsers(2000).during(100 seconds)).protocols(httpConf)
  )
}
