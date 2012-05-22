package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._


object Preview extends Controller {

  def analyze() = Action { implicit request =>
    val energySeries = (Date(), 1500.0) :: Nil
    val proteinSeries = (Date(), 1500.0) :: Nil
    val fatSeries = (Date(), 1500.0) :: Nil
    val carbsSeries = (Date(), 1500.0) :: Nil
    val weightSeries = (Date(), 1500.0) :: Nil
    val startDate = Date()
    val endDate = Date()
    Ok(views.html.previewAnalyze(energySeries, proteinSeries, fatSeries, carbsSeries, startDate.day, startDate.month, startDate.year, endDate.day, endDate.month, endDate.year, weightSeries))
  }

  def measure() = TODO

}

