package phoneBook

import java.sql.{Time, Timestamp}

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class Phones(tag: Tag) extends Table[PhoneData](tag, "phonebook") {
  def id = column[String]("ID", O.PrimaryKey)

  def name = column[String]("NAME")

  def number = column[String]("NUMBER")

  def editDate = column[Timestamp]("DATE")

  def * = (id, name, number, editDate) <> ((PhoneData.apply _).tupled, PhoneData.unapply)
}
