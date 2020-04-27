package phoneBook

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.collection.mutable
import scala.collection.mutable.Map
import scala.concurrent.Future

final case class PhoneData(id: PostId, name: String, number: String)

class PostId private (val underlying: String) extends AnyVal {
  override def toString: String = underlying.toString
}

object PostId {
  def apply(raw: String): PostId = {
    require(raw != null)
    new PostId(raw)
  }
}

class PostExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")


trait PhoneDataRepository {
  def create(data: PhoneData)(implicit mc: MarkerContext): Future[PostId]

  def list()(implicit mc: MarkerContext): Future[Iterable[PhoneData]]

  def delete(id: PostId): Future[PostId]

  def get(id: PostId)(implicit mc: MarkerContext): Future[Option[PhoneData]]
}


@Singleton
class PostRepositoryInMemory @Inject()()(implicit ec: PostExecutionContext)
    extends PhoneDataRepository {

  private val logger = Logger(this.getClass)

  private val phoneDataStorage : mutable.Map[PostId, PhoneData] = mutable.Map()

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[PhoneData]] = {
    Future {
      logger.trace(s"storage: ")
      phoneDataStorage.values
    }
  }

  override def get(id: PostId)(
      implicit mc: MarkerContext): Future[Option[PhoneData]] = {
    Future {
      logger.trace(s"get: id = $id")
      phoneDataStorage.get(id)
    }
  }

  override def create(data: PhoneData)(implicit mc: MarkerContext): Future[PostId] = {
    Future {
      logger.trace(s"create: data = $data")
      phoneDataStorage.addOne(data.id, data)
      data.id
    }
  }

  override def delete(id: PostId): Future[PostId] = {
    Future {
      logger.trace(s"delete: id = $id")
      phoneDataStorage.remove(id)
      id
    }
  }
}
