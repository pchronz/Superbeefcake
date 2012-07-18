import play.api._
import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    // bootstrap users
    if(Beefcake.all().isEmpty) {
      Logger.info("Bootstrapping beefcakes")
      Beefcake.create(Beefcake("peter", "secret", "peter.chronz@gmail.com", false))
      val michael = Beefcake("michael", "secret", "m@localhost.net", false)
      Beefcake.create(michael)
      // Boostrap a few entries for Michael
      val date = Date()
      (1 to 11).foreach{i => Logger.info("adding new macroentry in bootstrap"); MacroEntry.create(MacroEntry(None, None, Date(date.day - 1, date.month, date.year), 100, 100, 30, 5, 30), michael)}
      (1 to 9).foreach{i => Logger.info("adding new macroentry in bootstrap"); MacroEntry.create(MacroEntry(None, None, Date(), 100, 100, 30, 5, 30), michael)}
      (1 to 8).foreach{i => Logger.info("adding new macroentry in bootstrap"); MacroEntry.create(MacroEntry(None, None, Date(date.day + 1, date.month, date.year), 100, 100, 30, 5, 30), michael)}
    }
    // Bootstrap foods
    if(Food.all().isEmpty) {
      Logger.info("Bootstrapping foods")
      loadManualEntries()
      //parseUsdaArsFoods()
      parseGesAbFoods()
    }
    Logger.info("Following foods are in the DB...")
    Logger.info("There are " + Food.all().length + " food entries in the database.")

  }  

  def parseUsdaArsFoods() {
    // USDA ARS
    val nutrientsFile = io.Source.fromFile(play.api.Play.current.classloader.getResource("foods/ABBREV.txt").getFile)
    println(nutrientsFile.toString)
    val MacroEntryRegex = """~.*~\^~(.*)~\^[^\^]*\^([^\^]*)\^([^\^]*)\^([^\^]*)\^[^\^]*\^([^\^]*)\^.*""".r
    nutrientsFile.getLines().foreach{line =>
      val MacroEntryRegex(name, energy, protein, fat, carbs) = line
      try {
        val foodItem = Food(name, energy.toInt, protein.toDouble, fat.toDouble, carbs.toDouble)
        Food.create(foodItem)
      }
      catch { 
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def parseGesAbFoods() {
    // Ges Ab
    val nutrientsFile = io.Source.fromFile(play.api.Play.current.classloader.getResource("foods/GesAb.txt").getFile)
    println(nutrientsFile.toString)
    // XXX too imperative
    var successCounter = 0
    var totalCounter = 0
    val MacroEntryRegex = """([^@]+)@([^@]+)@([^@]+)@([^@]+)@([^@]+)@([^@]+)""".r
    val lines = nutrientsFile.getLines()
    lines.foreach{line =>
      try {
        val MacroEntryRegex(name, amount, energy, protein, fat, carbs) = line
        val AmountMatcher = """(\d+)\s*([^@]*)[^\w]*""".r
        val AmountMatcher(quantity, unit) = amount
        // adjust field to other standard weights
        val energyNum = energy.replaceAll(",", ".").toDouble
        val proteinNum = protein.replaceAll(",", ".").toDouble
        val fatNum = fat.replaceAll(",", ".").toDouble
        val carbsNum = carbs.replaceAll(",", ".").toDouble
        if(unit == "g") {
          val ratio = 1.0/quantity.toDouble
          val foodItem = Food(name, (ratio*energyNum).toInt, ratio*proteinNum, ratio*fatNum, ratio*carbsNum)
          Food.create(foodItem)
          successCounter += 1
        }
        else {
          println("Could not insert item " + name + " because of an unknown unit: " + unit)
        }
        totalCounter += 1
      }
      catch { 
        case e: Exception => e.printStackTrace()
      }
    }
    println("Got " + successCounter + " entries out of " + totalCounter + " lines in the file")
  }

  def loadManualEntries() {
      // Paula's collection
      Food.create(Food("Haehnchenbrustfilet", 87, 18.1, 1, 1.5))
      Food.create(Food("Burgi Bratkartoffeln", 81, 2.1, 1.4, 15.1))
      Food.create(Food("Bio Roastbeef", 130, 27, 2, 0))
      Food.create(Food("Trauben", 71, 0, 0, 15))
      Food.create(Food("Milch 1.5", 47, 3.4, 1.5, 4.9))
      Food.create(Food("Kasein-Shake", 368, 80, 0, 10))
      Food.create(Food("Huetten Kaese", 182, 23.4, 7.8, 4.6))
      Food.create(Food("Broccoli", 113, 5, 3, 14))
      Food.create(Food("Heidelbeere", 40, 0, 0, 8.5))
      Food.create(Food("Apfel", 55, 0, 0, 11))
      Food.create(Food("Becel Fett", 670, 0, 74, 0))
      Food.create(Food("Gruene Bohnen", 26, 1.6, 0, 3.1))
      Food.create(Food("Erdbeeren", 20, 0, 0, 11))
      Food.create(Food("Burgi Salzkartoffeln", 70, 1.9, 0, 14.8))
      Food.create(Food("Pomodori Pelati", 21, 1.2, 0, 3.8))
      Food.create(Food("Ei ganz", 154, 12.9, 11.2, 0))
      Food.create(Food("Eiklar", 50, 11, 0, 1))
      Food.create(Food("Spaghetti De Cecco", 352, 13, 1.5, 70))
      Food.create(Food("Speisequark Mager", 67, 12.2, 0, 3.9))
      Food.create(Food("Honig", 300, 0, 0, 75.1))
      Food.create(Food("Macadamia Crème", 671, 10.6, 57.8, 25.6))
      Food.create(Food("X-Treme Protein XXL Bar", 385, 31, 10, 42))
      Food.create(Food("Krakauer", 264, 13.8, 23.4, 0))
      Food.create(Food("BCAA", 57, 98, 0, 1.5))
      Food.create(Food("Partytomate", 18, 0.9, 0, 2.5))
      Food.create(Food("Orange", 40, 0, 0, 8))
      Food.create(Food("Lammfilet", 186, 27.3, 8.5, 0))
      Food.create(Food("Frosta Gemuesepfanne Thai", 39, 1.4, 0.9, 4.8))
      Food.create(Food("Monster Energy", 48, 9, 9, 12))
      Food.create(Food("Frosta India Tandori", 109, 6.33, 3.1, 13.1))
      Food.create(Food("Frosta Mediterrana", 41, 1.5, 3.3, 3.9))
      Food.create(Food("Demeter Mango Sorbet", 119, 0.3, 2, 28.9))
      Food.create(Food("Weider Lemon Curd", 376, 83.2, 2, 5.5))
      Food.create(Food("Weider Banana", 376, 83.2, 2, 5.5))
      Food.create(Food("Weider Vanilla", 376, 83.2, 2, 5.5))
      Food.create(Food("Frosta Rigatoni", 111, 4.3, 3.1, 17.3))
      Food.create(Food("Frosta Huehnchen Curry", 109, 6.3, 2.1, 16.1))
      Food.create(Food("Zur", 23, 0.8, 0.4, 4))
      Food.create(Food("Weisswurst", 230, 6.6, 25, 0))
      Food.create(Food("Deutscher Kaviar", 62, 8, 2, 2))
      Food.create(Food("Hirsch", 113, 20.6, 3.4, 0))
      Food.create(Food("Spaetzle", 335, 10, 1, 70))
      Food.create(Food("Gemuesesuppe", 35, 0, 2.19, 2.85))
      Food.create(Food("Champignonsauce", 02, 2.3, 6.9, 5.3))
      Food.create(Food("Preiselbeeren Konfituere", 270, 0, 0, 65))
      Food.create(Food("Schlagsahne", 288, 2.5, 30, 3.2))
      Food.create(Food("Hackfleisch Rind", 120, 21, 13, 0))
      Food.create(Food("Tomatenmark", 49, 2.2, 0, 5.6))
      Food.create(Food("Kefir", 49, 3.7, 1.5, 4.4))
      Food.create(Food("Canellini", 100, 6.5, 0, 17))
      Food.create(Food("Goldmais Mix", 109, 3.6, 1.6, 18.2))
      Food.create(Food("Champignon", 19, 2.2, 0, 0))
      Food.create(Food("Espresso Karamel Schokolade", 570, 7.6, 35.2, 48))
      Food.create(Food("Ayran", 34, 1.7, 1.9, 2.6))
      Food.create(Food("Toffifee", 535, 6, 31, 58))
      Food.create(Food("Vollmichschokolade", 525, 6, 30, 55))
      Food.create(Food("Brownie", 333, 6.3, 15, 43))
      Food.create(Food("Laktosefreie Milch", 36, 3.5, 0.3, 4.8))
      Food.create(Food("Reismilch", 46, 0, 1, 9.2))
      Food.create(Food("Frosta Karibik", 45, 1.3, 1.6, 5.4))
      Food.create(Food("Joey's Azzuro Classic", 257, 9.8, 11, 29.8))
      Food.create(Food("Edelbitter Citrus Pfeffer", 615, 6.1, 33, 47.5))
      Food.create(Food("Putenschnitzel", 161, 30, 4, 0))
      Food.create(Food("Hot Blonde Brownie", 52600, 710, 3340, 5020))
      Food.create(Food("Subway Steak", 32600, 4420, 760, 3640))
      Food.create(Food("Subway Frischkaese", 10600, 460, 900, 200))
      Food.create(Food("Subway Chapotle", 18000, 0, 1820, 300))
      Food.create(Food("Joghurt 0.1", 41, 4.6, 0, 5.4))
      Food.create(Food("Subway Ham Sandwich", 26600, 1710, 400, 4000))
      Food.create(Food("Subway Cookie", 22100, 230, 1170, 2650))
      Food.create(Food("Broetchen", 252, 7.8, 1, 51))
      Food.create(Food("Pizza", 199, 10, 5, 28))
      Food.create(Food("Haagen Dazs", 226, 4, 14.7, 19.5))
      Food.create(Food("Berliner", 300, 5.3, 7.3, 53.1))
      Food.create(Food("Feta Light", 210, 20, 14, 1))
      Food.create(Food("Moevenpick Amarena Kirsch", 170, 1.7, 3.6, 31.7))
      Food.create(Food("Passiert Tomaten", 22, 1.5, 0, 3.8))
      Food.create(Food("KitKat", 510, 7.1, 26, 60.8))
      Food.create(Food("Lion", 496, 4.5, 24, 65))
      Food.create(Food("Pfluemli", 208, 0.9, 0, 48))
      Food.create(Food("Muffin", 499, 5, 25, 51))
      Food.create(Food("Huettenkaese light", 90, 12.5, 4, 1))
      Food.create(Food("Mozarella Light", 184, 20, 0,11 ))
      Food.create(Food("Erdnussmus", 626, 30, 50, 94))
      Food.create(Food("Reis", 349, 6.83, 0, 77))
      Food.create(Food("Almighurt Mohn Marzpian", 144, 4.3, 4.1, 15))
      Food.create(Food("Banane", 95, 1, 0, 21))
      Food.create(Food("Aprikose Konfituere", 241, 0, 0, 58))
      Food.create(Food("Krakowska", 173, 23, 9, 0))
      Food.create(Food("Knoedel", 308, 8.7, 5.6, 54.7))
      Food.create(Food("Rotkohl", 25, 1.5, 0, 7.6))
      Food.create(Food("Rind Entrecote", 130, 22.4, 4.5, 0))
      Food.create(Food("Frosta Thai Green Curry", 115, 4, 3.8, 15.3))
      Food.create(Food("Landliebe Joghurt", 100, 3.5, 2.8, 15))
      Food.create(Food("Roast Beef", 130, 22.4, 4.5, 0))
      Food.create(Food("Zuckerruebensirup", 299, 2.3, 0.1, 0.69))
  }
  
  override def onStop(app: Application) {
    Logger.info("Application is done for")
  }  
    
}

