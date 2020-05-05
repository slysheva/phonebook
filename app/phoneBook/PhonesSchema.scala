package phoneBook

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global


class Phones(tag: Tag) extends Table[PhoneData](tag, "phones") {
  def id = column[String]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def number=  column[String]("NUMBER")

  def * = (id, name, number) <> ((PhoneData.apply _).tupled, PhoneData.unapply)
}
