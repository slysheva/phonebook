package phoneBook

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class PostFormInput(name: String, phoneNumber: String)


class PhoneBookController @Inject()(cc: PostControllerComponents)(
    implicit ec: ExecutionContext)
    extends PostBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[PostFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "name" -> nonEmptyText,
        "phoneNumber" -> text
      )(PostFormInput.apply)(PostFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("index: ")
    postResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def delete(id: String): Action[AnyContent] = PostAction.async {
    implicit request =>
      logger.trace(s"delete: id = $id")
      postResourceHandler.delete(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  def show(id: String): Action[AnyContent] = PostAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      postResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  def showFiltered: Action[AnyContent] = PostAction.async {
    implicit request =>
      val nameSubstring = request.getQueryString("nameSubstring").getOrElse(default = "")
      val phoneSubstring = request.getQueryString("phoneSubstring").getOrElse(default = "")
      logger.trace(s"showFiltered: nameSubstring = $nameSubstring phoneSubstring = $phoneSubstring")
      postResourceHandler.findBySubstring(nameSubstring, phoneSubstring).map { posts =>
        Ok(Json.toJson(posts))
      }
  }

  private def processJsonPost[A]()(
      implicit request: PhoneBookRequest[A]): Future[Result] = {
    def failure(badForm: Form[PostFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PostFormInput) = {
      postResourceHandler.create(input).map { post =>
        Created(Json.toJson(post))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
