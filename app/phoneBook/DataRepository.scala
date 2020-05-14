package phoneBook

import java.sql.{Time, Timestamp}
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.{Calendar, Date}

import scala.util.{Failure, Success, Try}
import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.{Await, Future}
import scala.collection.concurrent.{Map, TrieMap}
import scala.concurrent.duration._
import slick.jdbc.PostgresProfile.api._

final case class PhoneData(id: String, name: String, number: String, editDate: Timestamp)

class PostExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

trait DataRepository[A] {
  def createOrUpdate(data: A)(implicit mc: MarkerContext): Future[String]

  def list()(implicit mc: MarkerContext): Future[Iterable[A]]

  def delete(id: String)(implicit mc: MarkerContext): Future[String]

  def get(id: String)(implicit mc: MarkerContext): Future[Option[A]]
}

trait Cache[K, A] {
  def Lookup(key: K) : Option[A]
  def Remove(key: K) : Unit
  def FillCache(key:K, value: A) : Unit
}

trait LimitedThreadSafeTTLCache[K, A] extends Cache[K, A] {
  // A concurrent hash-trie or TrieMap is a concurrent thread-safe lock-free implementation of a hash array mapped trie.
  val internalStorage: TrieMap[K, (A, Instant)] = TrieMap()
  val TTL = 100000 //MILLIS
  val STORAGE_SIZE = 1000

  def cleanupCache(): Unit = {
    // clean some cache entities when cache grows too big
    // as we use hash based map we have no clue abort elem, so we simply drop first ones
    internalStorage.drop(internalStorage.size - STORAGE_SIZE)
  }

  def addToInternal(key: K, data: A): Unit = {
    if (internalStorage.size >= STORAGE_SIZE)
      cleanupCache()
    internalStorage(key) = (data, Instant.now())
  }

  override def Remove(key: K): Unit = {
    internalStorage.remove(key)
  }
  override def Lookup(key: K): Option[A] = {
    internalStorage.get(key) match {
      case Some(value) =>
        val gap = ChronoUnit.MILLIS.between(value._2, Instant.now())
        if (gap < TTL) {
          Some(value._1)
        } else None
      case None =>
        Option.empty
    }
  }

  override def FillCache(key: K, value: A): Unit = {
    addToInternal(key, value)
    cleanupCache()
  }
}

@Singleton
class PostRepositoryInDB @Inject()()(implicit ec: PostExecutionContext)
    extends DataRepository[PhoneData] {

  private val logger = Logger(this.getClass)

  val db_ = Database.forConfig("pgDB")
  val phones = TableQuery[Phones]
  val setup = DBIO.seq(
    phones.schema.createIfNotExists
  )

  val operationTimeout: FiniteDuration = 5.seconds

  {
    Await.result(db_.run(setup), operationTimeout)
  }

  override def list()(
    implicit mc: MarkerContext): Future[Iterable[PhoneData]] = {
    Future {
      logger.trace(s"listing storage: ")
      Await.result(db_.run(phones.result),operationTimeout)
    }
  }

  override def get(id: String)(
    implicit mc: MarkerContext): Future[Option[PhoneData]] = {
    Future {
      logger.trace(s"get: id = $id")
      val query = phones.filter(_.id === id)
      Await.result(db_.run(query.result), operationTimeout).headOption
    }
  }

  override def createOrUpdate(data: PhoneData)(implicit mc: MarkerContext): Future[String] = {
    Future {
      logger.trace(s"insert or update: data = $data")
      val q = phones.insertOrUpdate(data)
      Await.result(db_.run(q), operationTimeout)
      data.id
    }
  }

  override def delete(id: String)(implicit mc: MarkerContext): Future[String] = {
    Future {
      logger.trace(s"delete: id = $id")
      val q = phones.filter(_.id === id)
      val a = q.delete
      Option(Await.result(db_.run(a), operationTimeout))
      id
    }
  }
}

@Singleton
class CachingRepo @Inject()()(implicit ec: PostExecutionContext)
  extends PostRepositoryInDB with LimitedThreadSafeTTLCache[String, PhoneData] {

  override def get(id: String)(implicit mc: MarkerContext): Future[Option[PhoneData]] = {
    val cacheLookup = super.Lookup(id)
    cacheLookup match {
      case Some(value) => Future {
        Some(value)
      }
      case None => super.get(id)
    }
  }

  override def list()(implicit mc: MarkerContext): Future[Iterable[PhoneData]] = {
    val res = super.list()
    res.map(maybePhoneData =>
      maybePhoneData.foreach(phoneData =>
        super.FillCache(phoneData.id, phoneData)
      )
    )
    res
  }

  override def createOrUpdate(data: PhoneData)(implicit mc: MarkerContext): Future[String] = Future {
    val res = Await.result(super.createOrUpdate(data), operationTimeout)
    super.FillCache(data.id, data)
    res
  }
  override def delete(id: String)(implicit mc: MarkerContext): Future[String] = Future {
    super.Remove(id)
    super.delete(id)
    id
  }
}
