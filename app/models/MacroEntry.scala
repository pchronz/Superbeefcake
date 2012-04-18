package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Date(day: Int, month: Int, year: Int) {
  def toLong(): Long = {
    val cal = java.util.Calendar.getInstance
    // +1 for all retarded Oracle engineers make the month 0-based although everything else is 1-based
    cal.set(year, month+1, day, 0, 0)
    cal.getTimeInMillis
  }
}

object Date {
  def apply():Date = {
      // set to today
      val cal = java.util.Calendar.getInstance
      val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
      val month = cal.get(java.util.Calendar.MONTH) + 1
      val year = cal.get(java.util.Calendar.YEAR)
      Date(day, month, year)
  }

  def apply(time: Long): Date = {
      val cal = java.util.Calendar.getInstance
      cal.setTimeInMillis(time)
      val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
      val month = cal.get(java.util.Calendar.MONTH) + 1
      val year = cal.get(java.util.Calendar.YEAR)
      Date(day, month, year)
  }
}

case class MacroEntry(id: Option[Int], food: Option[String], time: Date, kCal: Double, protein: Double, fat: Double, carbs: Double)

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
        val entryName = food.name + " (" + amount.toString + ")"
        val kCal = food.kCal * amount / 100
        val protein = food.protein * amount / 100
        val fat = food.fat * amount / 100
        val carbs = food.carbs * amount / 100
        Some(MacroEntry.apply(id, Some(entryName), timeConcrete, kCal, protein, fat, carbs))
      }
    }
  }

  def apply(id: Option[Int], time: Option[Date], name: Option[String], kCal: Double, protein: Double, fat: Double, carbs: Double): MacroEntry = {
    val timeConcrete = time match {
      case None => Date()
      case Some(t) => t
    }
    MacroEntry.apply(id, name, timeConcrete, kCal, protein, fat, carbs)
  }

  val macroEntry = {
    get[Int]("id") ~
    get[Option[String]]("food") ~
    get[Int]("day") ~
    get[Int]("month") ~
    get[Int]("year") ~
    get[Double]("kCal") ~
    get[Double]("protein") ~
    get[Double]("fat") ~
    get[Double]("carbs") map {
      case id ~ food ~ day ~ month ~ year ~ kCal ~ protein ~ fat ~ carbs => {
          MacroEntry(Some(id), food, Date(day, month, year), kCal, protein, fat, carbs)
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
        SQL("INSERT INTO macroEntry (username, day, month, year, kcal, protein, fat, carbs) VALUES ({username}, {day}, {month}, {year}, {kCal}, {protein}, {fat}, {carbs})").on("username"->beefcake.username, "day" -> macroEntry.time.day, "month" -> macroEntry.time.month, "year" -> macroEntry.time.year, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
      case (Some(id), None) => {
        DB.withConnection{ implicit c =>
          SQL("INSERT INTO macroEntry (username, id, day, month, year, kcal, protein, fat, carbs) VALUES ({username}, {id}, {day}, {month}, {year}, {kCal}, {protein}, {fat}, {carbs})").on(
              "username"->beefcake.username, "id" -> id, "day" -> macroEntry.time.day, "month"->macroEntry.time.month, "year"->macroEntry.time.year, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
      case (None, Some(food)) => {
        DB.withConnection{ implicit c =>
          SQL("INSERT INTO macroEntry (username, food, day, month, year, kcal, protein, fat, carbs) VALUES ({username}, {food}, {day}, {month}, {year}, {kCal}, {protein}, {fat}, {carbs})").on(
              "username"->beefcake.username, "food"->food, "day" -> macroEntry.time.day, "month"->macroEntry.time.month, "year"->macroEntry.time.year, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
      case (Some(id), Some(food)) => {
        DB.withConnection{ implicit c =>
          SQL("INSERT INTO macroEntry (username, id, food, day, month, year, kcal, protein, fat, carbs) VALUES ({username}, {id}, {food}, {day}, {month}, {year}, {kCal}, {protein}, {fat}, {carbs})").on(
              "username"->beefcake.username, "id"->id, "food"->food, "day" -> macroEntry.time.day, "month"->macroEntry.time.month, "year"->macroEntry.time.year, "kCal" -> macroEntry.kCal, "protein" -> macroEntry.protein, "fat" -> macroEntry.fat, "carbs" -> macroEntry.carbs
          ).executeUpdate()
        }
      }
    }
  }

  def deleteAll(beefcake: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("DELETE FROM macroEntry * WHERE username = {username}").on("username"->beefcake.username).executeUpdate()
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

  def getTimeSeries(field: String, beefcake: Beefcake): List[(Date, Double)] = {
    DB.withConnection{ implicit c =>
        // anorm's on-function is simply incapable. total crap. who wrote this?
        val groupField = SQL("SELECT SUM(" + field + "), day, month, year FROM macroEntry WHERE username = {username} GROUP BY day, month, year").on("username"->beefcake.username)
        groupField().map{ row => 
            val date = Date(row[Int]("day"), row[Int]("month"), row[Int]("year"))
            date -> row[Double]("SUM(" + field + ")")
        }.toList
    }
  }
}

// vim: set ts=4 sw=4 et:
