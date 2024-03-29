package models

case class Date(day: Int, month: Int, year: Int) extends Ordered[Date] {
  def toLong(): Long = {
    val cal = java.util.Calendar.getInstance
    // +1 for all retarded Oracle engineers make the month 0-based although everything else is 1-based
    cal.set(year, month-1, day, 0, 0)
    cal.getTimeInMillis
  }

  def compare(that: Date) = {
    ((this.toLong - that.toLong)/1000).toInt
  }

  override def toString(): String = {
    day + "." + month + "." + year
  }

  def +(days: Int) = {
    Date(this.toLong + days.toLong * 24.toLong * 60.toLong * 60.toLong * 1000.toLong)
  }

  def -(days: Int) = {
    Date(this.toLong - days.toLong * 24.toLong * 60.toLong * 60.toLong * 1000.toLong)
  }
}

object Date extends Ordering[Date] {
  def apply():Date = {
      // set to today
      val cal = java.util.Calendar.getInstance
      val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
      val month = cal.get(java.util.Calendar.MONTH) + 1
      val year = cal.get(java.util.Calendar.YEAR)
      Date(day, month, year)
  }

  def apply(time: Long): Date = {
      val cal = java.util.Calendar.getInstance
      cal.setTimeInMillis(time)
      val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
      val month = cal.get(java.util.Calendar.MONTH) + 1
      val year = cal.get(java.util.Calendar.YEAR)
      Date(day, month, year)
  }

  def compare(x: Date, y: Date): Int = {
    ((x.toLong - y.toLong)/1000).toInt
  }
}

// vim: set ts=4 sw=4 et:
