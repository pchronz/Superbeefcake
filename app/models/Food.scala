package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Food(name: String, kCal: Double, protein: Double, fat: Double, carbs: Double)

object Food {
  val food = {
    get[String]("name") ~
    get[Double]("kCal") ~
    get[Double]("protein") ~
    get[Double]("fat") ~
    get[Double]("carbs") map {
      case name ~ kCal ~ protein ~ fat ~ carbs => Food(name, kCal, protein, fat, carbs)
    }
  }

  def create(food: Food) {
    DB.withConnection { implicit c =>
        SQL("INSERT INTO food (name, kCal, protein, fat, carbs) VALUES ({name}, {kCal}, {protein}, {fat}, {carbs})").on("name"->food.name, "kCal"->food.kCal, "protein"->food.protein, "fat"->food.fat, "carbs"->food.carbs).executeUpdate()
    }
  }

  def all(): List[Food] = {
      DB.withConnection{ implicit c =>
        SQL("SELECT * FROM food").as(food *)
      }
  }

  def deleteAll() {
    DB.withConnection{ implicit c =>
      SQL("DELETE FROM food *").executeUpdate()
    }
  }

  def findByName(name: String): Option[Food] = {
    val foods = DB.withConnection { implicit c =>
        SQL("SELECT * FROM food WHERE name = {name}").on("name"->name).as(food *)
    }
    foods.length match {
        case 0 => None
        case 1 => Some(foods(0))
        case _ => println("Found multiple food entries for name: " + name); Some(foods(0))
    }
  }

  def suggestFor(query: String): List[Food] = {
    DB.withConnection { implicit c =>
        SQL("SELECT * FROM food WHERE name LIKE {query}").on("query"->(query+"%")).as(food *)
    }
  }
}

// vim: set ts=4 sw=4 et:
