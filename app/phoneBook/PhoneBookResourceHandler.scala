package phoneBook

import javax.inject.{Inject, Provider}

import play.api.MarkerContext
import java.util.UUID.randomUUID

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._


case class PhoneBookResource(id: String, name: String, number: String)

object PhoneBookResource {
    implicit val format: Format[PhoneBookResource] = Json.format
}


class PhoneBookResourceHandler @Inject()(
                                     routerProvider: Provider[appRouter],
                                     phoneDataRepository: PhoneDataRepository)(implicit ec: ExecutionContext) {

  def create(postInput: PostFormInput)(
      implicit mc: MarkerContext): Future[PhoneBookResource] = {
    val data = PhoneData(randomUUID().toString, postInput.name, postInput.phoneNumber)

    phoneDataRepository.createOrUpdate(data).map { _ =>
      createPhoneBookResource(data)
    }
  }

  def update(id: String, postInput: PostFormInput)(
    implicit mc: MarkerContext): Future[PhoneBookResource] = {
    val data = PhoneData(id, postInput.name, postInput.phoneNumber)

    phoneDataRepository.createOrUpdate(data).map { _ =>
      createPhoneBookResource(data)
    }
  }

  def delete(id: String)(
    implicit mc: MarkerContext): Future[Option[PhoneBookResource]] = {
    val postFuture = phoneDataRepository.get(id)
    phoneDataRepository.delete(id)
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPhoneBookResource(postData)
      }
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[PhoneBookResource]] = {
    val postFuture = phoneDataRepository.get(id)
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPhoneBookResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[PhoneBookResource]] = {
    phoneDataRepository.list().map { postDataList =>
      postDataList.map(postData => createPhoneBookResource(postData))
    }
  }

  def findBySubstring(nameSubstring: String, phoneSubstring: String)(implicit mc: MarkerContext): Future[Iterable[PhoneBookResource]] = {
    phoneDataRepository.list().map { postDataList =>
      postDataList
          .filter(
            phoneData => phoneData.name.toLowerCase().contains(nameSubstring.toLowerCase())
              && phoneData.number.contains(phoneSubstring.toLowerCase()))
          .map(postData => createPhoneBookResource(postData))
    }
  }

  private def createPhoneBookResource(p: PhoneData): PhoneBookResource = {
//    PhoneBookResource(p.id.toString, routerProvider.get.link(p.id), p.name, p.number)
    PhoneBookResource(p.id, p.name, p.number)

  }
}
