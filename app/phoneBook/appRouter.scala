package phoneBook

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._


class appRouter @Inject()(controller: PhoneBookController) extends SimpleRouter {
  val prefix = "/phone"

  def link(id: String): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/phones") =>
      controller.index

    case POST(p"/phones/createNewPhone") =>
      controller.process

    case DELETE(p"/phone/$id") =>
      controller.delete(id)

    case GET(p"/phone/$id") =>
      controller.show(id)

    case GET(p"/phones/searchBySubstr") =>
      controller.showFiltered()
  }
}
