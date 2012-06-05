package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class MeasureGoal(field: String, goalValue: Double) {
}

object MeasureGoal {
  def create(measureGoal: MeasureGoal, beefcake: Beefcake) {
      DB.withConnection{ implicit c =>
        try {
          SQL("INSERT INTO measureGoal (username, field, goalValue) VALUES({username}, {field}, {goalValue})").on("username"->beefcake.username, "field"->measureGoal.field, "goalValue"->measureGoal.goalValue).executeUpdate()
        }
        catch {
          case e => 
            println("Error while inserting measure goal entry into DB ")
            println(e)
        }
      }
  }

  val measureGoal = {
    get[String]("field") ~
    get[Double]("goalValue") map {
      case field ~ goalValue =>
        MeasureGoal(field, goalValue)
    }
  }

  def all(beefcake: Beefcake): List[MeasureGoal] = {
    DB.withConnection{ implicit c =>
        SQL("SELECT * FROM measureGoal WHERE username = {username}").on("username"->beefcake.username).as(measureGoal *)
    }
  }

  def findByField(field: String, user: Beefcake) = {
    DB.withConnection{ implicit c =>
      val goals = SQL("SELECT * FROM measureGoal WHERE username = {username} AND field = {field}").on("username"->user.username, "field"->field).as(measureGoal *)
      goals.length match {
        case 0 => None
        case 1 => Some(goals(0))
        case _ =>
          println("Found multiple goals for user " + user + " and field " + field)
          Some(goals(0))
      }

    }
  }

  def deleteByGoal(goal: String, beefcake: Beefcake) {
     DB.withConnection {implicit c =>
         SQL("DELETE FROM measureGoal WHERE username = {username} AND goal = {goal}").on("username"->beefcake.username, "goal"->goal).executeUpdate()
     }
  }

  def update(measureGoal: MeasureGoal, user: Beefcake) {
    DB.withConnection{ implicit c =>
        SQL("UPDATE measureGoal SET goalValue={goalValue} WHERE username={username} AND field={field}").on("goalValue"-> measureGoal.goalValue, "field"->measureGoal.field, "username"->user.username).executeUpdate()
    }
  }
}

// vim: set ts=4 sw=4 et:
