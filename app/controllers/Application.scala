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

  def getUserFromSession(implicit session: Session): Beefcake = {
    def createAdhocUser(session: Session): Beefcake = {
      Logger.info("Creating new adhoc user")
      val user = Beefcake.createAdhoc()
      // give the Ad Hoc user a standard menu
      val date = Date()
      (1 to 11).foreach{i => MacroEntry.create(MacroEntry(None, None, Date(date.day - 1, date.month, date.year), 100, 100, 30, 5, 30), user)}
      user
    }

    // look for a registered user
    val regUser = session.get("beefcake") match {
      case Some(username) => {
          Logger.info("Got user from session: " + username)
          Beefcake.findByUsername(username) match {
            case None => None
            case user @ Some(_) => user
          }
      }
      case None => 
        Logger.info("Could not find registered user in session")
        None
    }

    // return user, registered, adhoc from session or new adhoc
    regUser match {
      case Some(user) => user
      case None => {
        Logger.info("Could not find registered user. Looking for Adhoc user...")
        session.get("beefcakeadhoc") match {
          case None => createAdhocUser(session)
          case Some(username) => {
            Beefcake.findByUsername(username) match {
              case None => createAdhocUser(session)
              case Some(user) => Logger.info("Found Adhoc user: " + user); user
            }
          }
        }
      }
    }
  }

  def index = Action { request =>
    // in case the user is already logged in
    getUserFromSession(request.session) match {
      case bc @ Beefcake(_, _, _, true, _) => {
        val macroEntries = MacroEntry.all(bc)
        Ok(views.html.index(macroEntries, macroEntryForm, macroEntryByFoodForm))
      }
      case user => Redirect(routes.Application.eat(None, None, None))    
    }
  }

  def eat(day: Option[Int], month: Option[Int], year: Option[Int]) = Action{implicit request =>
    getUserFromSession(session) match {
      case Beefcake(_, _, _, true, _)=> Redirect(routes.Application.index)
      case user => {
        (day, month, year) match {
          case (Some(d), Some(m), Some(y)) => {
            val macroEntries = MacroEntry.findByDate(Date(d, m, y), user)
            Ok(views.html.eat(day, month, year, macroEntries, macroEntryForm, macroEntryByFoodForm, dateFilterForm, user)).withSession(Map("day" -> d.toString, "month" -> m.toString, "year" -> y.toString).foldRight(request.session)((a,b) => b+a))
          }
          case _ => {
            val date = sessionToDate(request.session)
            val macroEntries = MacroEntry.findByDate(date, user)
            Ok(views.html.eat(day, month, year, macroEntries, macroEntryForm, macroEntryByFoodForm, dateFilterForm, user))
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
      "amount" -> number,
      "kCal" -> number,
      "protein" -> number,
      "fat" -> number,
      "carbs" -> number
    )
  )

  def submitMacroEntry = Action { implicit request =>
    val user = getUserFromSession(session)
    macroEntryForm.bindFromRequest.fold(
      {form => 
        Logger.error("binding errors for new macro entry")
        Logger.error(form.errors.mkString(", ")) 
        redirectFailed(user)
      },
      {fields => 
        val food = fields._1
        val day = fields._2
        val month = fields._3
        val year = fields._4
        val amount= fields._5
        val kCal = fields._6
        val protein = fields._7
        val fat = fields._8
        val carbs = fields._9
        val date = sessionToDate(session)
        val macroEntry = MacroEntry(None, time=Some(date), name=food, amount=amount, kCal=kCal, protein=protein, fat=fat, carbs=carbs)
        MacroEntry.create(macroEntry, user)
        user match {
          case Beefcake(_, _, _, true, _) => Redirect(routes.Application.index).withSession(addAdhocUserToSession(user, session))
          case _ => Redirect(routes.Application.eat(Some(date.day), Some(date.month), Some(date.year)))
        }
      }
    )
  }

  val userFoodEntryForm = Form (
    tuple(
      "name" -> text,
      "amount" -> text,
      "kCal" -> text,
      "protein" -> text,
      "fat" -> text,
      "carbs" -> text,
      "redirectTo" -> text
    )
  )

  def submitUserFoodEntry = Action { implicit request =>
    getUserFromSession(session) match {
      case user @ Beefcake(_, _, _, true, _) => 
        // adding new entries only works for registered users
        Redirect(routes.Application.index).withSession(addAdhocUserToSession(user, session))
      case user => 
        userFoodEntryForm.bindFromRequest.fold(
          {form =>
            Logger.error("binding errors for new user food entry")
            Logger.error(form.errors.mkString(", "))
            redirectFailed(user)
          }, 
          {fields =>
            val name = fields._1
            val amount = stringToDouble(fields._2)
            val kCal = stringToDouble(fields._3)
            val protein = stringToDouble(fields._4)
            val fat = stringToDouble(fields._5)
            val carbs = stringToDouble(fields._6)
            val date = sessionToDate(session)

            val ratio = 100/amount

            // the new food
            val food = Food(name=name, kCal=(kCal*ratio).toInt, protein=protein*ratio, fat=fat*ratio, carbs=carbs*ratio)
            Food.addUserFood(food, user)
            Logger.info("Added new food (" + food.toString + ") for user " + user.toString)
            val redirectTo = fields._7
            Redirect(redirectTo)
          }
      )
    }
  }

  val dateFilterForm = Form (
    tuple(
      "start-day" -> number,
      "start-month" -> number,
      "start-year" -> number
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

  def deleteMacroEntry(id: Int) = Action { implicit request =>
    val user = getUserFromSession(session)
    Logger.info("Going to delete macroEntry with id == " + id)
    MacroEntry.deleteById(id, user)
    user match {
      case Beefcake(_, _, _, true, _)=> {
        Redirect(routes.Application.index).withSession(addAdhocUserToSession(user, session))
      }
      case user => {
        Redirect(routes.Application.eat(None, None, None)).withSession(session + ("beefcake"->user.username))
      }
    }
  }

  def redirectFailed(user: Beefcake) = {
    user match {
      case Beefcake(_, _, _, true, _) => Redirect(routes.Application.index)
      case _ => Redirect(routes.Application.eat(None, None, None))
    }
  }

  val macroEntryByFoodForm = Form(
    tuple(
      "foodId" -> number,
      "amount" -> number,
      "day" -> number,
      "month" -> number,
      "year" -> number
    )
  )

  def submitMacroEntryByFood = Action { implicit request =>
    val user = getUserFromSession(session)
    macroEntryByFoodForm.bindFromRequest.fold (
      {form => 
        Logger.error("Could not bind the form in the request")
        Logger.error(form.toString)
        redirectFailed(user)
      },
      {fields =>
        val (foodId, amount, day, month, year) = fields
        MacroEntry(None, Some(Date(day, month, year)), foodId, amount, Some(user)) match {
          case None => redirectFailed(user)
          case Some(macroEntry) => {
            MacroEntry.create(macroEntry, user)
            user match {
              case Beefcake(_, _, _, true, _) => Redirect(routes.Application.index).withSession(addAdhocUserToSession(user, session))
              case _ => Redirect(routes.Application.eat(Some(macroEntry.time.day), Some(macroEntry.time.month), Some(macroEntry.time.year)))
            }
          }
        }
      }
    )
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
            if(Beefcake.authenticate(user, password)) {
              Logger.info("User " + user.username + " just logged in successfully.")
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
    redirectWithDateFromSession(session).withSession(session - "beefcake")
  }

  // AJAX 
  def suggestFood(query: String) = Action {implicit request =>
    def replaceUmlauts(in: String): String = {
      in.replaceAll("ä", "&auml;").replaceAll("Ä", "&Auml;").replaceAll("Ü", "&Uuml;").replaceAll("ü", "&uuml;").replaceAll("Ö", "&Ouml;").replaceAll("ö", "&ouml;").replaceAll("ß", "&szlig;")
    }
    val user = getUserFromSession(session)
    val foodSuggestions = Food.suggestFor(replaceUmlauts(query), Some(user))

    val foodNames = foodSuggestions.map { suggestion =>
      // using the name and replacing quotation marks to sanitize the JSON-based response to the client
      "{\"name\": \"" + suggestion.name.replace("\"", "\"") + "\"," + "\"id\": \"" + suggestion.id.get.toString + "\"}"
    }

    val response = "{\"items\": [" + foodNames.mkString(", ") + "]}"
    Logger.info(response)
    Ok(response)
  }

  def analyze(sDay: Option[Int], sMonth: Option[Int], sYear: Option[Int], eDay: Option[Int], eMonth: Option[Int], eYear: Option[Int]) = Action { implicit request =>
    Logger.info("Analyze")
    getUserFromSession(session) match {
      case user @ Beefcake(_, _, _, true, _) => Logger.info("Redirecting with user : " + user); Redirect(routes.Preview.analyze(sDay, sMonth, sYear, eDay, eMonth, eYear)).withSession(addAdhocUserToSession(user, session))
      case user => {
        // get the period
        val sDate = (sDay, sMonth, sYear) match {
          case (Some(d), Some(m), Some(y)) => Date(d, m, y)
          case _ => MacroEntry.getMinDate(user) match {
            case None => Date()
            case Some(d) => d
          }
        }
        val eDate = (eDay, eMonth, eYear) match {
          case (Some(d), Some(m), Some(y)) => Date(d, m, y)
          case _ => MacroEntry.getMaxDate(user) match {
            case None => Date()
            case Some(d) => d
          }
        }
        // query the database
        val energySeries = MacroEntry.getTimeSeries("kCal", Some(sDate), Some(eDate), user)
        val proteinSeries = MacroEntry.getTimeSeries("protein", Some(sDate), Some(eDate), user)
        val fatSeries = MacroEntry.getTimeSeries("fat", Some(sDate), Some(eDate), user)
        val carbsSeries = MacroEntry.getTimeSeries("carbs", Some(sDate), Some(eDate), user)
        val weightSeries = MeasureEntry.getTimeSeries("weight", Some(sDate), Some(eDate), user)
        val goalStrings = List("energy", "weight")
        // query goals and add them to the goals map if found
        val goalList = for(goalString<-goalStrings; val goal = MeasureGoal.findByField(goalString, user); if(goal != None)) 
          yield (goalString -> goal.get)
        val goals = new scala.collection.immutable.HashMap[String, MeasureGoal]() ++ goalList
        Ok(views.html.analyze(energySeries=energySeries, proteinSeries=proteinSeries, fatSeries=fatSeries, carbsSeries=carbsSeries, beefcake=user, startDay=sDate.day, startMonth=sDate.month, startYear=sDate.year, endDay=eDate.day, endMonth=eDate.month, endYear=eDate.year, weightSeries=weightSeries, goals=goals))
      }
    }
  }


  val analyzeDateForm = Form(
    tuple(
      "start-day"->number,
      "start-month"->number,
      "start-year"->number,
      "end-day"->number,
      "end-month"->number,
      "end-year"->number
    )
  )
  def analyzePost() = Action { implicit request =>
    Logger.info("Analyze post")
    analyzeDateForm.bindFromRequest.fold (
      {form =>  
        Logger.error("Could not bind analyze date from request")
        Redirect(routes.Application.analyze(None, None, None, None, None, None))
      }
      ,{fields => 
        Redirect(routes.Application.analyze(Some(fields._1), Some(fields._2), Some(fields._3), Some(fields._4), Some(fields._5), Some(fields._6)))
      }
    )
  }

  def register() = Action { implicit request =>
    Ok(views.html.register()).withNewSession
  }

  val registrationForm = Form(
    tuple(
      "username"->nonEmptyText,
      "password"->nonEmptyText,
      "passwordRepeat"->nonEmptyText,
      "email"->nonEmptyText,
      "betaKey"->number
    )
  )

  def registerUser() = Action{implicit request =>
    getUserFromSession(session) match {
      case Beefcake(_, _, _, true, _) => {
        registrationForm.bindFromRequest.fold(
          {form => Redirect(routes.Application.register)
          },
          {fields => 
            val username = fields._1
            val password = fields._2
            val passwordRepeat = fields._3
            val email = fields._4
            val betaKey = fields._5
            if(betaKey % 13 != 0) Redirect(routes.Application.register)
            if(password != password) Redirect(routes.Application.register)
            if(password.length < 4) Redirect(routes.Application.register)
            val Email = """\w+@\w+\.[a-zA-Z]+""".r
            Email.findFirstIn(email) match {
              case None => Redirect(routes.Application.register)
              case Some(_) => {
                if(Beefcake.exists(username, email)) {
                  Redirect(routes.Application.index)
                }
                else {
                  Beefcake.create(Beefcake(username=username, password=password, email=email, adhoc=false))
                  Redirect(routes.Application.login)
                }
              }
            }
          }
        )
      }
      case user => Redirect(routes.Application.login)
    }
  }

  def deleteAllEntries(day: Int, month: Int, year: Int) = Action { implicit request =>
    getUserFromSession(session) match {
      case user @ Beefcake(_, _, _, true, _) => {
        MacroEntry.deleteAll(user)
        Redirect(routes.Application.index).withSession(addAdhocUserToSession(user, session))
      }
      case user => {
        MacroEntry.deleteByDate(Date(day, month, year), user)
        Redirect(routes.Application.eat(Some(day), Some(month), Some(year)))
      }
    }
  }

  def updateMacroEntry(id: Int, amount: Int) = Action { implicit request =>
    val user = getUserFromSession(session)
    // get the entry
    MacroEntry.findById(id, user) match {
      // TODO how to return a 404?
      case None => Ok("")
      case Some(macroEntry) => {
        // udpate the fields
        try {
          val ratio = amount.toDouble / macroEntry.amount.toDouble
          val newAmount = amount
          val newEnergy = macroEntry.kCal * ratio
          val newProtein = macroEntry.protein * ratio
          val newFat = macroEntry.fat * ratio
          val newCarbs = macroEntry.carbs * ratio
          // update the entry
          MacroEntry.update(MacroEntry(macroEntry.id, macroEntry.food, macroEntry.time, newAmount, newEnergy, newProtein, newFat, newCarbs), user)
          val jsonCoreString = Map("energy"->newEnergy, "protein"->newProtein, "fat"->newFat, "carbs"->newCarbs).map{(value) => 
            "\"" + value._1 + "\"" + ": " + value._2.toInt.toString
          }.mkString(", ")
          Ok("[{" + jsonCoreString + "}]")
        }
        catch {
          case e =>
            Logger.error(e.toString)
            // TODO 404
            Ok("")
        }
      }
    }
  }

  def measure(startDay: Option[Int], startMonth: Option[Int], startYear: Option[Int], endDay: Option[Int], endMonth: Option[Int], endYear: Option[Int]) = Action { implicit request =>
    val startDate = (startDay, startMonth, startYear) match {
      case (Some(d), Some(m), Some(y)) => Some(Date(d, m, y))
      case _ => None
    }
    val endDate = (endDay, endMonth, endYear) match {
      case (Some(d), Some(m), Some(y)) => Some(Date(d, m, y))
      case _ => None
    }
    val user = getUserFromSession(session)
    (startDate, endDate) match {
      case (Some(s), Some(e)) => 
        val measureEntries = MeasureEntry.findByDates(s, e, user)
        Ok(views.html.measure(user, startDate, endDate, measureEntryForm, measureEntries))
      case _ => 
        val measureEntries = MeasureEntry.all(user)
        def compareMeasureEntries(a: MeasureEntry, b: MeasureEntry): Boolean = {
          (a.time, b.time) match {
            case (Some(tA), Some(tB)) => tA < tB
            case _ => true
          }
        }
        val sortedEntries = measureEntries.sortWith(compareMeasureEntries)
        sortedEntries match {
          case Nil => Ok(views.html.measure(user, None, None, measureEntryForm, measureEntries))
          case _ => Ok(views.html.measure(user, sortedEntries.head.time, sortedEntries.last.time, measureEntryForm, measureEntries))
        }
    }
  }

  val measureEntryForm = Form(
    tuple(
      "field"->nonEmptyText,
      "value"->nonEmptyText,
      "day"->number,
      "month"->number,
      "year"->number
    )
  )

  def submitMeasureEntry() = Action {implicit request =>
    Logger.info(request.body.toString)
    val user = getUserFromSession(session)
    measureEntryForm.bindFromRequest.fold( 
        form => Logger.warn("Binding error for measure entry: " + form.errors.map{_.message}.mkString(",")), 
        fields => {
          Logger.info("Adding " + fields)
          // convert the value to a double... why is there no proper decimal type for forms?
          val valueDouble = stringToDouble(fields._2)

          MeasureEntry.create(MeasureEntry(id=None, time=Some(Date(fields._3, fields._4, fields._5)), field=fields._1, value=valueDouble), user)
        }
      
    )
    user match {
      case Beefcake(_, _, _, true, _) => Redirect(routes.Application.measure(None, None, None, None, None, None)).withSession(addAdhocUserToSession(user, session))
      case _ => Redirect(routes.Application.measure(None, None, None, None, None, None))
    }
  }
  
  def deleteMeasureEntries(jsonIds: String) = Action{implicit request =>
    implicit def jsonStringToIntList(json: String): List[Int] = {
      val NumberListParser = """\d+""".r
      val numbers = NumberListParser findAllIn json
      numbers.map(_.toInt).toList
    }
    val user = getUserFromSession(session)
    Logger.info("Going to delete measureEntry with ids == " + jsonIds)
    jsonIds.foreach(MeasureEntry.deleteById(_, user))
    user match {
      case Beefcake(_, _, _, true, _)=> {
        Logger.warn("Adhoc user just tried to delete a MeasureEntry!")
        Redirect(routes.Application.measure(None, None, None, None, None, None)).withSession(addAdhocUserToSession(user, session))
      }
      case user => {
        Redirect(routes.Application.measure(None, None, None, None, None, None)).withSession(session + ("beefcake"->user.username))
      }
    }
  }

  def updateMeasureEntry(id: Int, field: String, value: String) = Action {implicit request =>
    val user = getUserFromSession(session)
    // get the entry
    MeasureEntry.findById(id, user) match {
      // TODO how to return a 404?
      case None => Ok("")
      case Some(measureEntry) => 
        try {
          val numValue = value.replaceAll(",", ".").toDouble
          MeasureEntry.update(MeasureEntry(id=Some(id), time=measureEntry.time, field="weight", value=numValue), user)
          Ok("")
        }
        catch {
          case e =>
            Logger.error("Error while trying to update measureEntry.id == " + id + " with value == " + value)
            Logger.error(e.toString)
          // TODO 404
          Ok("")
        }
    }
  }

  val measureDateFilterForm = Form(
    tuple(
      "start-day"->number,
      "start-month"->number,
      "start-year"->number,
      "end-day"->number,
      "end-month"->number,
      "end-year"->number
    )
  )

  def submitMeasureDateFilter() = Action{ implicit request =>
    val user = getUserFromSession(session)
    measureDateFilterForm.bindFromRequest.fold(
      {form => 
        Logger.error("Could not bind measure date fields on post")
        Redirect(routes.Application.measure(None, None, None, None, None, None))
      },
      {fields =>
        Redirect(routes.Application.measure(Some(fields._1), Some(fields._2), Some(fields._3), Some(fields._4), Some(fields._5), Some(fields._6)))
      }
    ) 
  }

  def addAdhocUserToSession(user: Beefcake, session: Session): Session = {
    session + ("beefcakeadhoc"->user.username)
  }

  def updateGoal(field: String, value: String) = Action{implicit request =>
    val user = getUserFromSession(session)
    val newGoal = stringToDouble(value)
    MeasureGoal.update(MeasureGoal(field, newGoal), user)
    Ok("success")
  }

  private def stringToDouble(decimal: String) = {
    decimal.replace(",", ".").toDouble
  }

  def manageOwnFoodEntries = Action { implicit request =>
    val user = getUserFromSession(session)
    val ownFoods = Food.listUserFood(user)
    Ok(views.html.ownFoods(ownFoods, user))
  }

  def deleteOwnFood(id: Int) = Action { implicit request =>
    val user = getUserFromSession(session)
    Food.deleteById(id, user)
    Redirect(routes.Application.manageOwnFoodEntries())
  }

  val updateFoodEntryForm = Form (
    tuple(
      "name"->text,
      "amount"->number,
      "energy"->number,
      "protein"->text,
      "fat"->text,
      "carbs"->text,
      "id"->number
    )
  )

  def updateFood(id: Option[Int], name: Option[String], amount: Option[Int], energy: Option[Int], protein: Option[String], fat: Option[String], carbs: Option[String]) = Action { implicit request =>
    val user = getUserFromSession(session)
    (id, name, amount, energy, protein, fat, carbs) match {
      case (Some(id), Some(name), Some(amount), Some(energy), Some(protein), Some(fat), Some(carbs)) =>
        Logger.info("Updating food entry " + id)
        // XXX ineffcient: performing a query and then the insert or update
        // this is supposed to work as one operation but seems to be vendor-specific (on duplicate key, unique constraint, ...)
        val multiplier = if(amount != 100) 100.0/amount else 1.0
        val newFood = Food(id=Some(id), name=name, kCal=(energy*multiplier).toInt, protein=stringToDouble(protein)*multiplier, fat=stringToDouble(fat)*multiplier, carbs=stringToDouble(carbs)*multiplier)
        Logger.info("New Food: " + newFood)
        Food.update(newFood, user)
      case _ =>
        Logger.warn("Update food could not be performed due to mising parameters")
        Logger.warn("Name: " + name)
        Logger.warn("Amount: " + amount)
        Logger.warn("Energy: " + energy)
        Logger.warn("Protein: " + protein)
        Logger.warn("Fat: " + fat)
        Logger.warn("Carbs: " + carbs)
    }
    Ok(name.getOrElse(""))
  }

  def changePasswordView = Action { implicit request =>
    Ok(views.html.changePassword())
  }

  val changePasswordForm = Form(
    tuple(
      "old-password"->text,
      "new-password"->text,
      "new-password-confirmation"->text
    )
  )
  def changePassword = Action { implicit request =>
    val user = getUserFromSession
    changePasswordForm.bindFromRequest.fold(
      {form =>
        Logger.error("Could not bind change password form")
        Redirect(routes.Application.deauth)
      },
      {fields =>
        val oldPassword = fields._1
        val newPassword = fields._2
        val newPasswordConf = fields._3
        if(newPassword != newPasswordConf) {
          Ok(views.html.changePassword())
        }
        else {
          if(Beefcake.authenticate(user, oldPassword)) {
            Beefcake.changePassword(user, newPassword)
            Redirect(routes.Application.deauth)
          }
          else {
            Ok(views.html.changePassword())
          }
        }
      }
    )
  }

  def administer = Action { implicit request =>
    val user = getUserFromSession(session)
    if(user.username == "peter") {
      val users = Beefcake.all()
      val registeredUsers = for(u<-users if !u.adhoc) yield u
      Ok(views.html.administer(user, registeredUsers))
    }
    else {
      Redirect(routes.Application.index)
    }
  }

  val newUserForm = Form (
    tuple(
      "username" -> text,
      "password" -> text
    )
  )

  def submitNewUser = Action { implicit request =>
    newUserForm.bindFromRequest.fold(
      {form =>
        Logger.error("Error while binding new user request")
      },
      {fields =>
        val username = fields._1
        val password = fields._2
        // create a new user
        Beefcake.create(Beefcake(username, password, "foo@foo.com", false))
      }
    )
    Redirect(routes.Application.administer())
  }
}

