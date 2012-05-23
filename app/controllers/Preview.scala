package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._


object Preview extends Controller {

  def analyze(sDay: Option[Int], sMonth: Option[Int], sYear: Option[Int], eDay: Option[Int], eMonth: Option[Int], eYear: Option[Int]) = Action { implicit request =>
    // TODO filter dates 
    // TODO shift actual date by some between the past and today
    // TODO generate values using a probabilistic model
    // mock the energy series
    val iteratorList = 1 to 50
    val energySeries = iteratorList.map(it => (Date() + it, 2300.0))
    val proteinSeries = iteratorList.map(it => (Date() + it, 250.0))
    val fatSeries = iteratorList.map(it => (Date() + it, 25.0))
    val carbsSeries = iteratorList.map(it => (Date() + it, 150.0))
    val weightSeries = iteratorList.map(it => (Date() + it, 98.0 - 0.025 * it.toDouble))
    val startDate = Date()
    val endDate = Date()
    Ok(views.html.previewAnalyze(energySeries, proteinSeries, fatSeries, carbsSeries, startDate.day, startDate.month, startDate.year, endDate.day, endDate.month, endDate.year, weightSeries))
  }

  def analyzePost() = Action{ implicit request =>
    Application.analyzeDateForm.bindFromRequest.fold (
      {form =>  
        Logger.error("Could not bind analyze date from request")
        Redirect(routes.Preview.analyze(None, None, None, None, None, None))
      }
      ,{fields => 
        Redirect(routes.Preview.analyze(Some(fields._1), Some(fields._2), Some(fields._3), Some(fields._4), Some(fields._5), Some(fields._6)))
      }
    )
  }

  def measure() = TODO

}

