import javax.inject._

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import phoneBook._

class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {

  override def configure(): Unit = {
    bind[DataRepository[PhoneData]].to[CachingRepo].in[Singleton] //Caching
  }
}