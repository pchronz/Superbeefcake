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

  // DB
  def create(food: Food) {
    DB.withConnection { implicit c =>
        SQL("INSERT INTO food (name, kCal, protein, fat, carbs) VALUES ({name}, {kCal}, {protein}, {fat}, {carbs})").on("name"->food.name, "kCal"->food.kCal, "protein"->food.protein, "fat"->food.fat, "carbs"->food.carbs).executeUpdate()
    }
  }

  def addUserFood(food: Food, user: Beefcake) {
    DB.withConnection { implicit c =>
        SQL("INSERT INTO food (name, kCal, protein, fat, carbs, username) VALUES ({name}, {kCal}, {protein}, {fat}, {carbs}, {username})").on("name"->food.name, "kCal"->food.kCal, "protein"->food.protein, "fat"->food.fat, "carbs"->food.carbs, "username"->user.username).executeUpdate()
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

  def findByName(name: String, user: Option[Beefcake]): Option[Food] = {
    val foods = DB.withConnection { implicit c =>
        SQL("SELECT * FROM food WHERE name = {name}").on("name"->name).as(food *)
    }
    foods.length match {
        case 0 => None
        case 1 => Some(foods(0))
        case _ => println("Found multiple food entries for name: " + name); Some(foods(0))
    }
  }

  def suggestFor(query: String, user: Option[Beefcake]): List[Food] = {
    val upperQuery = query.toUpperCase
    // split by whitespace
    val keywords = upperQuery.split(" ")
    // prepare the query
    var intersectQuery = keywords.map{keyword =>
      "SELECT * FROM food WHERE UCASE(name) LIKE '%" + keyword + "%'"
    }.mkString("\nINTERSECT\n")
    intersectQuery += "\nLIMIT 8"
    val results = DB.withConnection { implicit c =>
        //SQL("SELECT * FROM food WHERE name LIKE {query} LIMIT 8").on("query"->("%"+upperQuery+"%")).as(food *)
        SQL(intersectQuery).as(food *)
    }
    println("#results: " + results.length)
    println(results.mkString(", "))
    results
  }
}

// vim: set ts=4 sw=4 et:
