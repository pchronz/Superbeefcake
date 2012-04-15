package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  private def sessionToDate(session: Session): Date = {
    val day = session.get("day").map { day =>
      day
    }
    val month = session.get("month").map { month =>
      month
    }
    val year = session.get("year").map { year =>
      year
    }
    (day, month, year) match {
      case (Some(d), Some(m), Some(y)) => Date(d.toInt, m.toInt, y.toInt)
      case _ => Date()
    }
  }

  private def getUserFromSession(session: Session): Option[Beefcake] = {
    session.get("beefcake") match {
      case Some(username) => Logger.info("Got user from session: " + username); Beefcake.findByUsername(username)
      case None => Logger.info("No user in session scope."); None
    }
  }

  def index = Action { request =>
    // in case the user is already logged in
    getUserFromSession(request.session) match {
      case None => Redirect(routes.Application.login)
      case Some(user) => Redirect(routes.Application.eat(None, None, None))    
    }
  }

  def eat(day: Option[Int], month: Option[Int], year: Option[Int]) = Action{implicit request =>
    getUserFromSession(session) match {
      case None => Redirect(routes.Application.login)
      case Some(user) => {
        (day, month, year) match {
          case (Some(d), Some(m), Some(y)) => {
            val macroEntries = MacroEntry.findByDate(Date(d, m, y), user)
            Ok(views.html.eat(day, month, year, macroEntries, macroEntryForm, macroEntryByFoodForm, dateFilterForm)).withSession(Map("day" -> d.toString, "month" -> m.toString, "year" -> y.toString).foldRight(request.session)((a,b) => b+a))
          }
          case _ => {
            val date = sessionToDate(request.session)
            val macroEntries = MacroEntry.findByDate(date, user)
            Ok(views.html.eat(Some(date.day), Some(date.month), Some(date.year), macroEntries, macroEntryForm, macroEntryByFoodForm, dateFilterForm))
          }
        }
      }
    }
  }
  
  val macroEntryForm = Form(
    tuple(
      "food" -> optional(text),
      "day" -> number,
      "month" -> number,
      "year" -> number,
      "kCal" -> number,
      "protein" -> number,
      "fat" -> number,
      "carbs" -> number
    )
  )

  def submitMacroEntry = Action { implicit request =>
    getUserFromSession(request.session) match {
      case None => Ok(views.html.index())
      case Some(user) =>{
        macroEntryForm.bindFromRequest.fold(
          {form => 
            Logger.error("binding errors for new macro entry")
            Logger.error(form.errors.mkString(", ")) 
            Redirect(routes.Application.eat(None, None, None))
          },
          {fields => 
            val food = fields._1
            val day = fields._2
            val month = fields._3
            val year = fields._4
            val kCal = fields._5
            val protein = fields._6
            val fat = fields._7
            val carbs = fields._8
            Logger.info(fields.toString)
            MacroEntry.create(MacroEntry(None, time=Some(Date(day, month, year)), name=food, kCal=kCal, protein=protein, fat=fat, carbs=carbs), user)
            Redirect(routes.Application.eat(Some(day), Some(month), Some(year)))
          }
        )
      }
    }
  }

  val dateFilterForm = Form (
    tuple(
      "day" -> number,
      "month" -> number,
      "year" -> number
    )
  )

  def submitDateFilter = Action { implicit request =>
    dateFilterForm.bindFromRequest.fold(
      {errors => Logger.error("binding errors"); Date()
        Redirect(routes.Application.eat(None, None, None))
      },
      {values =>
        Logger.info("Filter date bound successfully")
        Redirect(routes.Application.eat(Some(values._1), Some(values._2), Some(values._3)))
      }
    )
  }

  def deleteEntry(id: Int) = Action { implicit request =>
    getUserFromSession(session) match {
      case None => Ok(views.html.index())
      case Some(user) => {
        Logger.info("Going to delete macroEntry with id == " + id)
        MacroEntry.deleteById(id, user)
        Redirect(routes.Application.eat(None, None, None)).withSession(session + ("beefcake"->user.username))
      }
    }
  }

  val macroEntryByFoodForm = Form(
    tuple(
      "foodName" -> text,
      "amount" -> number,
      "day" -> number,
      "month" -> number,
      "year" -> number
    )
  )

  def submitMacroEntryByFood = Action { implicit request =>
    getUserFromSession(session) match {
      case None => Ok(views.html.index())
      case Some(user) => {
        macroEntryByFoodForm.bindFromRequest.fold (
          {form => 
            Logger.error("Could not bind the form in the request")
            Logger.error(form.toString)
            Redirect(routes.Application.eat(None, None, None))
          },
          {fields =>
            val (foodName, amount, day, month, year) = fields
            MacroEntry(None, Some(Date(day, month, year)), foodName, amount) match {
              case None =>
              case Some(macroEntry) => MacroEntry.create(macroEntry, user)
            }
            Redirect(routes.Application.eat(Some(day), Some(month), Some(year)))
          }
        )
      }
    }
  }

  val loginForm = Form(
    tuple(
      "username"->text,
      "password"->text
    )
  )

  def auth = Action { implicit request =>
    loginForm.bindFromRequest.fold (
      {form =>
        Logger.error("User log failure")
        Redirect(routes.Application.login())
      },
      {fields =>
        val (username, password) = fields
        Beefcake.findByUsername(username) match {
          case None => {
            Logger.error("Username " + username + " not found")
            Redirect(routes.Application.login())
          }
          case Some(user) => {
            if(user.password == password) {
              Logger.info("User " + user.username + " just logged in successfully.")
              // TODO update with current date
              val date = Date()
              redirectWithDateFromSession(session).withSession(Map("beefcake"->user.username, "day"->date.day.toString, "month"->date.month.toString, "year"->date.year.toString).foldLeft(session)((a,b) => a+b))
            }
            else {
              Logger.info("User " + user.username + " tried to login with wrong password.")
              Redirect(routes.Application.login())
            }
          }
        }
      }
    )
  }

  def login() = Action { request =>
    Ok(views.html.login())
  }

  private def redirectWithDateFromSession(session: Session): SimpleResult[Results.EmptyContent] = {
      val date = sessionToDate(session)
      Redirect(routes.Application.eat(Some(date.day), Some(date.month), Some(date.year)))
  }
  
  def deauth() = Action { implicit request =>
    redirectWithDateFromSession(session).withNewSession
  }

  // AJAX 
  def suggestFood(query: String) = Action {implicit request =>
    val foodSuggestions = Food.suggestFor(query)

    val foodNames = foodSuggestions.map { suggestion =>
      // using the name and replacing quotation marks to sanitize the JSON-based response to the client
      suggestion.name.replace("\"", "\\\"")
    }

    val response = "{\"items\": [\"" + foodNames.mkString("\", \"") + "\"]}"
    Ok(response)
  }

  def analyze() = Action { implicit request =>
    getUserFromSession(session) match {
      case None => Redirect(routes.Application.login)
      case Some(user) => {
        val energySeries = MacroEntry.getTimeSeries("kCal", user)
        val proteinSeries = MacroEntry.getTimeSeries("protein", user)
        val fatSeries = MacroEntry.getTimeSeries("fat", user)
        val carbsSeries = MacroEntry.getTimeSeries("carbs", user)
        Ok(views.html.analyze(energySeries=energySeries, proteinSeries=proteinSeries, fatSeries=fatSeries, carbsSeries=carbsSeries))
      }
    }
  }
}

