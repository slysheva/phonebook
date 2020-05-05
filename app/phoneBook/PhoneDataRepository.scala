package phoneBook

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import slick.jdbc.PostgresProfile.api._


final case class PhoneData(id: String, name: String, number: String)

class PostExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")


trait PhoneDataRepository {
  def createOrUpdate(data: PhoneData)(implicit mc: MarkerContext): Future[String]

  def list()(implicit mc: MarkerContext): Future[Iterable[PhoneData]]

  def delete(id: String)(implicit mc: MarkerContext): Future[String]

  def get(id: String)(implicit mc: MarkerContext): Future[Option[PhoneData]]
}

@Singleton
class PostRepositoryInDB @Inject()()(implicit ec: PostExecutionContext)
    extends PhoneDataRepository {

  private val logger = Logger(this.getClass)

  val db_ = Database.forConfig("pgDB")

  val phones = TableQuery[Phones]

  val setup = DBIO.seq(
    phones.schema.createIfNotExists
  )

  {
    Await.result(db_.run(setup), 5.seconds)
  }

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[PhoneData]] = {
    Future {
      logger.trace(s"storage: ")
      val q = for(p <- phones) yield p
      val a = q.result
      Await.result(db_.run(a), 5.seconds)
    }
  }

  override def get(id: String)(
      implicit mc: MarkerContext): Future[Option[PhoneData]] = {
    Future {
      logger.trace(s"get: id = $id")
      val q = phones.filter(_.id === id)
      val a = q.result
      Await.result(db_.run(a), 5.seconds).headOption
    }
  }

  override def createOrUpdate(data: PhoneData)(implicit mc: MarkerContext): Future[String] = {
    Future {
      logger.trace(s"insert or update: data = $data")
      val q = phones.insertOrUpdate(data)
      Await.result(db_.run(q), 5.seconds)
      data.id
    }
  }

  override def delete(id: String)(implicit mc: MarkerContext): Future[String] = {
    Future {
      logger.trace(s"delete: id = $id")
      val q = phones.filter(_.id === id)
      val a = q.delete
      Option(Await.result(db_.run(a), 5.seconds))
      id
    }
  }

}
