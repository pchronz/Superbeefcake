package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.util.Random

case class Beefcake(username: String, password: String, email: String, adhoc: Boolean, lastUpdated: Date) {
  override def toString() = {
    username
  }
}

object Beefcake {
  def apply(username: String, password: String, email: String, adhoc: Boolean):Beefcake = {
    Beefcake(username, password, email, adhoc, Date())
  }

  // Anorm parser
  val beefcake = {
    get[String]("username") ~
    get[String]("password") ~ 
    get[String]("email") ~ 
    get[Boolean]("adhoc") ~ 
    get[Long]("lastUpdated") map {
      case username ~ password ~ email ~ adhoc ~ lastUpdated => {
        Beefcake(username, password, email, adhoc, Date(lastUpdated))
      }
    }
  }

  def all(): List[Beefcake] = DB.withConnection{ implicit c =>
      SQL("SELECT * FROM beefcake").as(beefcake *)
  }

  def create(beefcake: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("INSERT INTO beefcake (username, password, email, adhoc, lastUpdated) VALUES ({username}, {password}, {email}, {adhoc}, {lastUpdated})").on("username"->beefcake.username, "password"->beefcake.password, "email"->beefcake.email, "adhoc"->false, "lastUpdated"->new java.util.Date().getTime()).executeUpdate()
    }
  }

  def createAdhoc():Beefcake = {
    val random = new Random()
    val chars = List('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
    val username = (1 to 16).map{i => val num = random.nextInt(chars.length); chars(num)}.mkString("")
    println("username" + username)
    val email = random.nextString(8) + "@" + random.nextString(8) + ".com"
    DB.withConnection{ implicit c =>
        SQL("INSERT INTO beefcake (username, password, email, adhoc, lastUpdated) VALUES ({username}, 'password', {email}, {adhoc}, {lastUpdated})").on("username"->username, "email"->email, "adhoc"->true, "lastUpdated"->new java.util.Date().getTime()).executeUpdate()
        val beefcakes = SQL("SELECT * FROM beefcake WHERE username = {username}").on("username"->username).as(beefcake *)
        beefcakes(0)
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

  def exists(username: String, email: String): Boolean = {
    DB.withConnection{ implicit c =>
        val user = SQL("SELECT * FROM beefcake WHERE username = {username} OR email = {email}").on("username"->username, "email"->email)
        user().length > 0
    }
  }

  def changePassword(beefcake: Beefcake, newPassword: String) = {
      DB.withConnection { implicit c =>
          SQL("UPDATE beefcake SET password={password} WHERE username={username}").on("username"->beefcake.username, "password"->newPassword).executeUpdate()
      }
  }
}


// vim: set ts=4 sw=4 et:
