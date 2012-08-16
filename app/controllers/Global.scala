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
        val foodItem = Food(name=name, kCal=energy.toInt, protein=protein.toDouble, fat=fat.toDouble, carbs=carbs.toDouble)
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
          val ratio = 100.0/quantity.toDouble
          val foodItem = Food(name=name, kCal=(ratio*energyNum).toInt, protein=ratio*proteinNum, fat=ratio*fatNum, carbs=ratio*carbsNum)
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
      Food.create(Food(name="Haehnchenbrustfilet", kCal=87, protein=18.1, fat=1, carbs=1.5))
      Food.create(Food(name="Burgi Bratkartoffeln", kCal=81, protein=2.1, fat=1.4, carbs=15.1))
      Food.create(Food(name="Bio Roastbeef", kCal=130, protein=27, fat=2, carbs=0))
      Food.create(Food(name="Trauben", kCal=71, protein=0, fat=0, carbs=15))
      Food.create(Food(name="Milch 1.5", kCal=47, protein=3.4, fat=1.5, carbs=4.9))
      Food.create(Food(name="Kasein-Shake", kCal=368, protein=80, fat=0, carbs=10))
      Food.create(Food(name="Huetten Kaese", kCal=182, protein=23.4, fat=7.8, carbs=4.6))
      Food.create(Food(name="Broccoli", kCal=113, protein=5, fat=3, carbs=14))
      Food.create(Food(name="Heidelbeere", kCal=40, protein=0, fat=0, carbs=8.5))
      Food.create(Food(name="Apfel", kCal=55, protein=0, fat=0, carbs=11))
      Food.create(Food(name="Becel Fett", kCal=670, protein=0, fat=74, carbs=0))
      Food.create(Food(name="Gruene Bohnen", kCal=26, protein=1.6, fat=0, carbs=3.1))
      Food.create(Food(name="Erdbeeren", kCal=20, protein=0, fat=0, carbs=11))
      Food.create(Food(name="Burgi Salzkartoffeln", kCal=70, protein=1.9, fat=0, carbs=14.8))
      Food.create(Food(name="Pomodori Pelati", kCal=21, protein=1.2, fat=0, carbs=3.8))
      Food.create(Food(name="Ei ganz", kCal=154, protein=12.9, fat=11.2, carbs=0))
      Food.create(Food(name="Eiklar", kCal=50, protein=11, fat=0, carbs=1))
      Food.create(Food(name="Spaghetti De Cecco", kCal=352, protein=13, fat=1.5, carbs=70))
      Food.create(Food(name="Speisequark Mager", kCal=67, protein=12.2, fat=0, carbs=3.9))
      Food.create(Food(name="Honig", kCal=300, protein=0, fat=0, carbs=75.1))
      Food.create(Food(name="Macadamia Crème", kCal=671, protein=10.6, fat=57.8, carbs=25.6))
      Food.create(Food(name="X-Treme Protein XXL Bar", kCal=385, protein=31, fat=10, carbs=42))
      Food.create(Food(name="Krakauer", kCal=264, protein=13.8, fat=23.4, carbs=0))
      Food.create(Food(name="BCAA", kCal=57, protein=98, fat=0, carbs=1.5))
      Food.create(Food(name="Partytomate", kCal=18, protein=0.9, fat=0, carbs=2.5))
      Food.create(Food(name="Orange", kCal=40, protein=0, fat=0, carbs=8))
      Food.create(Food(name="Lammfilet", kCal=186, protein=27.3, fat=8.5, carbs=0))
      Food.create(Food(name="Frosta Gemuesepfanne Thai", kCal=39, protein=1.4, fat=0.9, carbs=4.8))
      Food.create(Food(name="Monster Energy", kCal=48, protein=9, fat=9, carbs=12))
      Food.create(Food(name="Frosta India Tandori", kCal=109, protein=6.33, fat=3.1, carbs=13.1))
      Food.create(Food(name="Frosta Mediterrana", kCal=41, protein=1.5, fat=3.3, carbs=3.9))
      Food.create(Food(name="Demeter Mango Sorbet", kCal=119, protein=0.3, fat=2, carbs=28.9))
      Food.create(Food(name="Weider Lemon Curd", kCal=376, protein=83.2, fat=2, carbs=5.5))
      Food.create(Food(name="Weider Banana", kCal=376, protein=83.2, fat=2, carbs=5.5))
      Food.create(Food(name="Weider Vanilla", kCal=376, protein=83.2, fat=2, carbs=5.5))
      Food.create(Food(name="Frosta Rigatoni", kCal=111, protein=4.3, fat=3.1, carbs=17.3))
      Food.create(Food(name="Frosta Huehnchen Curry", kCal=109, protein=6.3, fat=2.1, carbs=16.1))
      Food.create(Food(name="Zur", kCal=23, protein=0.8, fat=0.4, carbs=4))
      Food.create(Food(name="Weisswurst", kCal=230, protein=6.6, fat=25, carbs=0))
      Food.create(Food(name="Deutscher Kaviar", kCal=62, protein=8, fat=2, carbs=2))
      Food.create(Food(name="Hirsch", kCal=113, protein=20.6, fat=3.4, carbs=0))
      Food.create(Food(name="Spaetzle", kCal=335, protein=10, fat=1, carbs=70))
      Food.create(Food(name="Gemuesesuppe", kCal=35, protein=0, fat=2.19, carbs=2.85))
      Food.create(Food(name="Champignonsauce", kCal=02, protein=2.3, fat=6.9, carbs=5.3))
      Food.create(Food(name="Preiselbeeren Konfituere", kCal=270, protein=0, fat=0, carbs=65))
      Food.create(Food(name="Schlagsahne", kCal=288, protein=2.5, fat=30, carbs=3.2))
      Food.create(Food(name="Hackfleisch Rind", kCal=120, protein=21, fat=13, carbs=0))
      Food.create(Food(name="Tomatenmark", kCal=49, protein=2.2, fat=0, carbs=5.6))
      Food.create(Food(name="Kefir", kCal=49, protein=3.7, fat=1.5, carbs=4.4))
      Food.create(Food(name="Canellini", kCal=100, protein=6.5, fat=0, carbs=17))
      Food.create(Food(name="Goldmais Mix", kCal=109, protein=3.6, fat=1.6, carbs=18.2))
      Food.create(Food(name="Champignon", kCal=19, protein=2.2, fat=0, carbs=0))
      Food.create(Food(name="Espresso Karamel Schokolade", kCal=570, protein=7.6, fat=35.2, carbs=48))
      Food.create(Food(name="Ayran", kCal=34, protein=1.7, fat=1.9, carbs=2.6))
      Food.create(Food(name="Toffifee", kCal=535, protein=6, fat=31, carbs=58))
      Food.create(Food(name="Vollmichschokolade", kCal=525, protein=6, fat=30, carbs=55))
      Food.create(Food(name="Brownie", kCal=333, protein=6.3, fat=15, carbs=43))
      Food.create(Food(name="Laktosefreie Milch", kCal=36, protein=3.5, fat=0.3, carbs=4.8))
      Food.create(Food(name="Reismilch", kCal=46, protein=0, fat=1, carbs=9.2))
      Food.create(Food(name="Frosta Karibik", kCal=45, protein=1.3, fat=1.6, carbs=5.4))
      Food.create(Food(name="Joey's Azzuro Classic", kCal=257, protein=9.8, fat=11, carbs=29.8))
      Food.create(Food(name="Edelbitter Citrus Pfeffer", kCal=615, protein=6.1, fat=33, carbs=47.5))
      Food.create(Food(name="Putenschnitzel", kCal=161, protein=30, fat=4, carbs=0))
      Food.create(Food(name="Hot Blonde Brownie", kCal=52600, protein=710, fat=3340, carbs=5020))
      Food.create(Food(name="Subway Steak", kCal=32600, protein=4420, fat=760, carbs=3640))
      Food.create(Food(name="Subway Frischkaese", kCal=10600, protein=460, fat=900, carbs=200))
      Food.create(Food(name="Subway Chapotle", kCal=18000, protein=0, fat=1820, carbs=300))
      Food.create(Food(name="Joghurt 0.1", kCal=41, protein=4.6, fat=0, carbs=5.4))
      Food.create(Food(name="Subway Ham Sandwich", kCal=26600, protein=1710, fat=400, carbs=4000))
      Food.create(Food(name="Subway Cookie", kCal=22100, protein=230, fat=1170, carbs=2650))
      Food.create(Food(name="Broetchen", kCal=252, protein=7.8, fat=1, carbs=51))
      Food.create(Food(name="Pizza", kCal=199, protein=10, fat=5, carbs=28))
      Food.create(Food(name="Haagen Dazs", kCal=226, protein=4, fat=14.7, carbs=19.5))
      Food.create(Food(name="Berliner", kCal=300, protein=5.3, fat=7.3, carbs=53.1))
      Food.create(Food(name="Feta Light", kCal=210, protein=20, fat=14, carbs=1))
      Food.create(Food(name="Moevenpick Amarena Kirsch", kCal=170, protein=1.7, fat=3.6, carbs=31.7))
      Food.create(Food(name="Passiert Tomaten", kCal=22, protein=1.5, fat=0, carbs=3.8))
      Food.create(Food(name="KitKat", kCal=510, protein=7.1, fat=26, carbs=60.8))
      Food.create(Food(name="Lion", kCal=496, protein=4.5, fat=24, carbs=65))
      Food.create(Food(name="Pfluemli", kCal=208, protein=0.9, fat=0, carbs=48))
      Food.create(Food(name="Muffin", kCal=499, protein=5, fat=25, carbs=51))
      Food.create(Food(name="Huettenkaese light", kCal=90, protein=12.5, fat=4, carbs=1))
      Food.create(Food(name="Mozarella Light", kCal=184, protein=20, fat=0, carbs=11 ))
      Food.create(Food(name="Erdnussmus", kCal=626, protein=30, fat=50, carbs=94))
      Food.create(Food(name="Reis", kCal=349, protein=6.83, fat=0, carbs=77))
      Food.create(Food(name="Almighurt Mohn Marzpian", kCal=144, protein=4.3, fat=4.1, carbs=15))
      Food.create(Food(name="Banane", kCal=95, protein=1, fat=0, carbs=21))
      Food.create(Food(name="Aprikose Konfituere", kCal=241, protein=0, fat=0, carbs=58))
      Food.create(Food(name="Krakowska", kCal=173, protein=23, fat=9, carbs=0))
      Food.create(Food(name="Knoedel", kCal=308, protein=8.7, fat=5.6, carbs=54.7))
      Food.create(Food(name="Rotkohl", kCal=25, protein=1.5, fat=0, carbs=7.6))
      Food.create(Food(name="Rind Entrecote", kCal=130, protein=22.4, fat=4.5, carbs=0))
      Food.create(Food(name="Frosta Thai Green Curry", kCal=115, protein=4, fat=3.8, carbs=15.3))
      Food.create(Food(name="Landliebe Joghurt", kCal=100, protein=3.5, fat=2.8, carbs=15))
      Food.create(Food(name="Roast Beef", kCal=130, protein=22.4, fat=4.5, carbs=0))
      Food.create(Food(name="Zuckerruebensirup", kCal=299, protein=2.3, fat=0.1, carbs=0.69))
  }
  
  override def onStop(app: Application) {
    Logger.info("Application is done for")
  }  
    
}

