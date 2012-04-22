package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class MeasureEntry(id: Option[Int], time: Option[Date], field: String, value: Double)

object MeasureEntry {
  def create(measureEntry: MeasureEntry, beefcake: Beefcake) {
      DB.withConnection{ implicit c =>
          (measureEntry.id, measureEntry.time) match {
              case (None, None) =>
                val time = Date()
                  SQL("INSERT INTO measure (username, day, month, year, field, value) VALUES({username}, {day}, {month}, {year}, {field}, {value})").on("username"->beefcake.username, "day"->time.day, "month"->time.month, "year"->time.year, "field"->measureEntry.field, "value"->measureEntry.value).executeUpdate()
              case (Some(id), None) =>
                val time = Date()
                  SQL("INSERT INTO measure (id, username, day, month, year, field, value) VALUES({id}, {username}, {day}, {month}, {year}, {field}, {value})").on("id"->id, "username"->beefcake.username, "day"->time.day, "month"->time.month, "year"->time.year, "field"->measureEntry.field, "value"->measureEntry.value).executeUpdate()
              case (None, Some(time)) =>
                  SQL("INSERT INTO measure (username, day, month, year, field, value) VALUES({username}, {day}, {month}, {year}, {field}, {value})").on("username"->beefcake.username, "day"->time.day, "month"->time.month, "year"->time.year, "field"->measureEntry.field, "value"->measureEntry.value).executeUpdate()
              case (Some(id), Some(time)) =>
                  SQL("INSERT INTO measure (id, username, day, month, year, field, value) VALUES({id}, {username}, {day}, {month}, {year}, {field}, {value})").on("id"->id, "username"->beefcake.username, "day"->time.day, "month"->time.month, "year"->time.year, "field"->measureEntry.field, "value"->measureEntry.value).executeUpdate()
          }
      }
  }

  val measureEntry = {
    get[Int]("id") ~
    get[Int]("day") ~
    get[Int]("month") ~
    get[Int]("year") ~
    get[String]("field") ~
    get[Double]("value") map {
      case id ~ day ~ month ~ year ~ field ~ value =>
        MeasureEntry(Some(id), Some(Date(day, month, year)), field, value)
    }
  }

  def all(beefcake: Beefcake): List[MeasureEntry] = {
    DB.withConnection{ implicit c =>
        SQL("SELECT * FROM measure WHERE username = {username}").on("username"->beefcake.username).as(measureEntry *)
    }
  }
}

// vim: set ts=4 sw=4 et:
