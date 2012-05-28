package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class MeasureEntry(id: Option[Int], time: Option[Date], field: String, value: Double) {
}

object MeasureEntry {
  def create(measureEntry: MeasureEntry, beefcake: Beefcake) {
      DB.withConnection{ implicit c =>
        try {
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
          catch {
            case e => 
              println("Error while inserting measure entry into DB ")
              println(e)
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

  def deleteById(id: Int, beefcake: Beefcake) {
     DB.withConnection {implicit c =>
         SQL("DELETE FROM measure WHERE username = {username} AND id = {id}").on("username"->beefcake.username, "id"->id).executeUpdate()
     }
  }

  def getTimeSeries(field: String, start: Option[Date], end: Option[Date], beefcake: Beefcake):List[(Date, Double)] = {
      DB.withConnection{ implicit c =>
        // anorm's on-function is simply incapable. total crap. who wrote this?
        val groupField = SQL("SELECT value, day, month, year FROM measure WHERE username = {username} AND field = '" + field + "'").on("username"->beefcake.username)
        val allEntries = groupField().map{ row => 
            val date = Date(row[Int]("day"), row[Int]("month"), row[Int]("year"))
            date -> row[Double]("value")
        }.toList
        (start, end) match {
            case (Some(start), Some(end)) => 
              for(entry<-allEntries if entry._1 >= start; if entry._1 <= end) yield entry
            case (Some(start), None) =>
              for(entry<-allEntries if entry._1 >= start) yield entry
            case (None, Some(end)) =>
              for(entry<-allEntries if entry._1 <= end) yield entry
            case _ => allEntries
        }
      }
  }

  def findById(id: Int, user: Beefcake): Option[MeasureEntry] = {
    DB.withConnection{implicit c =>
        val measureEntries = SQL("SELECT * FROM measure WHERE id={id} AND username={username}").on("username"->user.username, "id"->id).as(measureEntry *)
        measureEntries.length match {
          case 0 => None
          case 1 => Some(measureEntries(0))
          case _ => 
            println("Found multiple measure entries for id" + id)
            Some(measureEntries(0))
        }
    }
  }

  def findByDates(startDate: Date, endDate: Date, user: Beefcake) = {
    DB.withConnection{ implicit c =>
        val measureEntries = SQL("SELECT * FROM measure WHERE year>={startYear} AND year<={endYear} AND username={username}").on("username"->user.username, "startYear"->startDate.year, "endYear"->endDate.year).as(measureEntry *)
        measureEntries.filter(measureEntry => measureEntry.time.get >= startDate && measureEntry.time.get <= endDate)
    }
  }

  def update(measureEntry: MeasureEntry, user: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("UPDATE measure SET value={value}, field={field} WHERE username={username} AND id={id}").on("value"-> measureEntry.value, "field"->measureEntry.field, "value"->measureEntry.value, "username"->user.username, "id"->measureEntry.id).executeUpdate()
    }
  }

  def renderIdsAsJson(measureEntries: List[MeasureEntry]): String = {
    measureEntries.length match {
      case 0 => "{[]}"
      case _ => measureEntries.init.foldLeft("{[")(_ + _.id.get.toString + ", ") + measureEntries.last.id.get + "]}"
    }
    
  }
}

// vim: set ts=4 sw=4 et:
