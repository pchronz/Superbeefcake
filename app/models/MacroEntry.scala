package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class MacroEntry(id: Option[Int], food: Option[String], time: Date, amount: Int, kCal: Double, protein: Double, fat: Double, carbs: Double)

object MacroEntry {
  def apply(id: Option[Int], time: Option[Date], foodName: String, amount: Int): Option[MacroEntry] = {
    val timeConcrete: Date = time match {
      case None => Date()
      case Some(t) => t
    }
    val food = Food.findByName(foodName) 
    food match {
      case None => None
      case Some(food) => {
        val entryName = food.name
        val kCal = food.kCal * amount / 100
        val protein = food.protein * amount / 100
        val fat = food.fat * amount / 100
        val carbs = food.carbs * amount / 100
        Some(MacroEntry.apply(id, Some(entryName), timeConcrete, amount, kCal, protein, fat, carbs))
      }
    }
  }

  def apply(id: Option[Int], time: Option[Date], name: Option[String], amount: Int, kCal: Double, protein: Double, fat: Double, carbs: Double): MacroEntry = {
    val timeConcrete = time match {
      case None => Date()
      case Some(t) => t
    }
    MacroEntry.apply(id, name, timeConcrete, amount, kCal, protein, fat, carbs)
  }

  val macroEntry = {
    get[Int]("id") ~
    get[Option[String]]("food") ~
    get[Int]("day") ~
    get[Int]("month") ~
    get[Int]("year") ~
    get[Int]("amount") ~
    get[Double]("kCal") ~
    get[Double]("protein") ~
    get[Double]("fat") ~
    get[Double]("carbs") map {
      case id ~ food ~ day ~ month ~ year ~ amount ~ kCal ~ protein ~ fat ~ carbs => {
          MacroEntry(Some(id), food, Date(day, month, year), amount, kCal, protein, fat, carbs)
      }
    }
  }

  def all(beefcake: Beefcake): List[MacroEntry] = DB.withConnection { implicit c =>
      SQL("SELECT * FROM macroEntry WHERE username = {username}").on("username"->beefcake.username).as(macroEntry *)
  }

  def create(macroEntry: MacroEntry, beefcake: Beefcake) {
    (macroEntry.id, macroEntry.food) match {
      case (None, None) => {
        DB.withConnection{ implicit c =>
        SQL("INSERT INTO macroEntry (username, day, month, year, amount, kcal, protein, fat, carbs) VALUES ({username}, {day}, {month}, {year}, {amount}, {kCal}, {protein}, {fat}, {carbs})").on("username"->beefcake.username, "day" -> macroEntry.time.day, "month" -> macroEntry.time.month, "year" -> macroEntry.time.year, "amount"->macroEntry.amount, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
      case (Some(id), None) => {
        DB.withConnection{ implicit c =>
          SQL("INSERT INTO macroEntry (username, id, day, month, year, amount, kcal, protein, fat, carbs) VALUES ({username}, {id}, {day}, {month}, {year}, {amount}, {kCal}, {protein}, {fat}, {carbs})").on(
              "username"->beefcake.username, "id" -> id, "day" -> macroEntry.time.day, "month"->macroEntry.time.month, "year"->macroEntry.time.year, "amount"->macroEntry.amount, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
      case (None, Some(food)) => {
        DB.withConnection{ implicit c =>
          SQL("INSERT INTO macroEntry (username, food, day, month, year, amount, kcal, protein, fat, carbs) VALUES ({username}, {food}, {day}, {month}, {year}, {amount}, {kCal}, {protein}, {fat}, {carbs})").on(
              "username"->beefcake.username, "food"->food, "day" -> macroEntry.time.day, "month"->macroEntry.time.month, "year"->macroEntry.time.year, "amount"->macroEntry.amount, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
      case (Some(id), Some(food)) => {
        DB.withConnection{ implicit c =>
          SQL("INSERT INTO macroEntry (username, id, food, day, month, year, amount, kcal, protein, fat, carbs) VALUES ({username}, {id}, {food}, {day}, {month}, {year}, {amount}, {kCal}, {protein}, {fat}, {carbs})").on(
              "username"->beefcake.username, "id"->id, "food"->food, "day" -> macroEntry.time.day, "month"->macroEntry.time.month, "year"->macroEntry.time.year, "amount"->macroEntry.amount, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
    }
  }

  def deleteAll(beefcake: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("DELETE FROM macroentry WHERE username = {username}").on("username"->beefcake.username).executeUpdate()
    }
  }

  def deleteByDate(date: Date, beefcake: Beefcake) {
      DB.withConnection {implicit c =>
      SQL("DELETE FROM macroentry WHERE day = {day} AND month = {month} AND year = {year} AND username = {username}").on("username"->beefcake.username, "day"->date.day, "month"->date.month, "year"->date.year).executeUpdate()
      }
  }

  def findByDate(date: Date, beefcake: Beefcake): List[MacroEntry] = DB.withConnection { implicit c =>
      SQL("SELECT * FROM macroEntry WHERE day = {day} AND month = {month} AND year = {year} AND username = {username}").on("username"->beefcake.username, "day"->date.day, "month"->date.month, "year"->date.year).as(macroEntry *)
  }

  def deleteById(id: Int, beefcake: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("DELETE FROM macroEntry WHERE id = {id} AND username = {username}").on("username"->beefcake.username, "id" -> id).executeUpdate()
    }
  }

  def getTimeSeries(field: String, start: Option[Date], end: Option[Date], beefcake: Beefcake): List[(Date, Double)] = {
    DB.withConnection{ implicit c =>
        // anorm's on-function is simply incapable. total crap. who wrote this?
        val groupField = SQL("SELECT SUM(" + field + "), day, month, year FROM macroEntry WHERE username = {username} GROUP BY day, month, year").on("username"->beefcake.username)
        val allEntries = groupField().map{ row => 
            val date = Date(row[Int]("day"), row[Int]("month"), row[Int]("year"))
            date -> row[Double]("SUM(" + field + ")")
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

  def findById(id: Int, beefcake: Beefcake): Option[MacroEntry] = {
      val entries = DB.withConnection{ implicit c =>
          SQL("SELECT * FROM macroEntry WHERE username = {username} AND id = {id}").on("username"->beefcake.username, "id"->id).as(macroEntry *)
      }
      entries.length match {
        case 1 => Some(entries(0))
        case 0 => None
        case _ => 
          println("Got multiple results for MacroEntry.id == " + id)
          None
      }
  }

  def update(macroEntry: MacroEntry, beefcake: Beefcake) {
      DB.withConnection{ implicit c =>
          SQL("UPDATE macroEntry SET amount={amount}, kCal={kCal}, protein={protein}, fat={fat}, carbs={carbs} WHERE username={username} AND id={id}").on("amount"->macroEntry.amount, "kCal"->macroEntry.kCal, "protein"->macroEntry.protein, "fat"->macroEntry.fat, "carbs"->macroEntry.carbs, "username"->beefcake.username, "id"-> macroEntry.id).executeUpdate()
      }
  }

  def getMaxDate(beefcake: Beefcake): Option[Date] = {
    DB.withConnection{ implicit c =>
      val dates = SQL("SELECT day, month, year FROM macroEntry WHERE username = {username}").on("username"->beefcake.username)
      val allDates = dates().map{ row =>
        Date(row[Int]("day"), row[Int]("month"), row[Int]("year"))
      }
      allDates.length match {
        case 0 => None
        case _ =>  Some(allDates.max(Date))
      }
    }
  }

  def getMinDate(beefcake: Beefcake): Option[Date] = {
    DB.withConnection{ implicit c =>
      val dates = SQL("SELECT day, month, year FROM macroEntry WHERE username = {username}").on("username"->beefcake.username)
      val allDates = dates().map{ row =>
        Date(row[Int]("day"), row[Int]("month"), row[Int]("year"))
      }
      allDates.length match {
        case 0 => None
        case _ =>  Some(allDates.min(Date))
      }
    }
  }
}

// vim: set ts=4 sw=4 et:
