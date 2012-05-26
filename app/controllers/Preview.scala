package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import scala.util.Random


object Preview extends Controller {

  def analyze(sDay: Option[Int], sMonth: Option[Int], sYear: Option[Int], eDay: Option[Int], eMonth: Option[Int], eYear: Option[Int]) = Action { implicit request =>
    // mock the energy series
    val r = new Random(42)
    val recordDuration = 50
    val iteratorList = (0 to recordDuration).reverse
    val energySeries = iteratorList.map(it => (Date() - it, 2300.0 + r.nextGaussian * 50))
    val proteinSeries = iteratorList.map(it => (Date() - it, 250.0 + r.nextGaussian * 15))
    val fatSeries = iteratorList.map(it => (Date() - it, 25.0 + r.nextGaussian * 5))
    val carbsSeries = iteratorList.map(it => (Date() - it, 150.0 + r.nextGaussian * 15))
    val weightSeries = iteratorList.map(it => (Date() - it, 98.0 + 0.025 * it.toDouble + r.nextGaussian * 0.1))
    val startDate = (sDay, sMonth, sYear) match {
      case (Some(sD), Some(sM), Some(sY)) => Date(sD, sM, sY)
      case _ => Date() - 7
    }
    val endDate = (eDay, eMonth, eYear) match {
      case (Some(eD), Some(eM), Some(eY)) => Date(eD, eM, eY)
      case _ => Date()
    }
    val energyFiltered = energySeries.filter((el: Tuple2[Date, Double]) => el._1.toLong >= startDate.toLong && el._1.toLong <= endDate.toLong)
    val proteinFiltered = proteinSeries.filter((el: Tuple2[Date, Double]) => el._1.toLong >= startDate.toLong && el._1.toLong <= endDate.toLong)
    val fatFiltered = fatSeries.filter((el: Tuple2[Date, Double]) => el._1.toLong >= startDate.toLong && el._1.toLong <= endDate.toLong)
    val carbsFiltered = carbsSeries.filter((el: Tuple2[Date, Double]) => el._1.toLong >= startDate.toLong && el._1.toLong <= endDate.toLong)
    val weightFiltered = weightSeries.filter((el: Tuple2[Date, Double]) => el._1.toLong >= startDate.toLong && el._1.toLong <= endDate.toLong)
    val (minDataDate, maxDataDate) = List(energyFiltered, proteinFiltered, fatFiltered, carbsFiltered, weightFiltered).foldLeft(List[Date]())((dates: List[Date], series) => series.map(_._1).toList ::: dates) match {
      case List() => (Date(), Date())
      case l => (l.minBy(_.toLong), l.maxBy(_.toLong))
    }
    Ok(views.html.previewAnalyze(energyFiltered, proteinFiltered, fatFiltered, carbsFiltered, minDataDate.day, minDataDate.month, minDataDate.year, maxDataDate.day, maxDataDate.month, maxDataDate.year, weightFiltered))
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

