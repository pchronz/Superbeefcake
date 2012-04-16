package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Beefcake(username: String, password: String, email: String)

object Beefcake {
  // Anorm parser
  val beefcake = {
    get[String]("username") ~
    get[String]("password") ~ 
    get[String]("email") map {
      case username ~ password ~ email => {
        Beefcake(username, password, email)
      }
    }
  }

  def all(): List[Beefcake] = DB.withConnection{ implicit c =>
      SQL("SELECT * FROM beefcake").as(beefcake *)
  }

  def create(beefcake: Beefcake) {
    DB.withConnection{ implicit c =>
    SQL("INSERT INTO beefcake (username, password, email) VALUES ({username}, {password}, {email})").on("username"->beefcake.username, "password"->beefcake.password, "email"->beefcake.email).executeUpdate()
    }
  }

  def findByUsername(username: String): Option[Beefcake] = DB.withConnection{ implicit c =>
      val users = SQL("SELECT * FROM beefcake WHERE username = {username}").on("username"->username).as(beefcake *)
      users.length match {
          case 0 => None
          case 1 => Some(users(0))
          case _ => println("More than one users found for username == " + username); Some(users(0))
      }
  }

  def delete(beefcake: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("DELETE FROM beefcake WHERE username = {username}").on("username"->beefcake.username).executeUpdate()
    }
  }
}


// vim: set ts=4 sw=4 et:
