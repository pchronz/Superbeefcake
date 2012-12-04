package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Food(id: Option[Int] = None, name: String, kCal: Int, protein: Double, fat: Double, carbs: Double) {
  override def toString() = {
    this.id + ": " + this.name
  }
}

object Food {
  val food = {
    get[Int]("id") ~
    get[String]("name") ~
    get[Int]("kCal") ~
    get[Double]("protein") ~
    get[Double]("fat") ~
    get[Double]("carbs") map {
        case id ~ name ~ kCal ~ protein ~ fat ~ carbs => Food(Some(id), name, kCal, protein, fat, carbs)
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

  def allUserFoods(): List[Food] = {
      DB.withConnection { implicit c =>
          SQL("SELECT * FROM food WHERE username IS NOT NULL").as(food *)
      }
  }

  def deleteAll() {
    DB.withConnection{ implicit c =>
      SQL("DELETE FROM food *").executeUpdate()
    }
  }

  def deleteById(id: Int, user: Beefcake) {
    DB.withConnection { implicit c =>
    SQL("DELETE FROM food WHERE id={id} AND username={username}").on("id"->id, "username"->user.username).executeUpdate()
    }
  }

  def getById(id: Int, user: Option[Beefcake]): Option[Food] = {
    val foods = DB.withConnection { implicit c =>
        SQL("SELECT * FROM food WHERE id = {id}").on("id"->id).as(food *)
    }
    foods.length match {
        case 0 => None
        case 1 => Some(foods(0))
        case _ => println("Found multiple food entries for id: " + id); Some(foods(0))
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

  def listUserFood(user: Beefcake): List[Food] = {
      DB.withConnection { implicit c =>
          SQL("SELECT * FROM food WHERE username = {username}").on("username"->user.username).as(food *)
      }
  }
  
  def update(food: Food, user: Beefcake) {
      food.id match {
          case None =>
          case Some(id) =>
              DB.withConnection { implicit c =>
                  SQL("UPDATE food SET name={name}, kCal={kCal}, protein={protein}, fat={fat}, carbs={carbs} WHERE username={username} AND id={id}").on("name"->food.name, "kCal"->food.kCal, "protein"->food.protein, "fat"->food.fat, "carbs"->food.carbs, "username"->user.username, "id"->food.id.get).executeUpdate()
          }
      }
  }

  def acceptFood(id: String) {
      DB.withConnection { implicit c =>
          SQL("UPDATE food set username = NULL WHERE id = {id}").on("id"->id).executeUpdate
      }
  }
}

// vim: set ts=4 sw=4 et:
