package alison.fivethingskotlin.Util

import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.FreeSpec
import io.kotlintest.matchers.shouldEqual
import java.util.*

class DatesTest: FreeSpec( {
    "converts from Date object" - {
        "to YY-MM-DD" {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, 2017)
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 22)
            val date = cal.time
            getDatabaseStyleDate(date) shouldEqual "17-01-22"
        }
    }

    "gets day of week" - {
        "from a date object" {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, 2017)
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 22)
            val date = cal.time
            getDayOfWeek(date) shouldEqual "Sunday"
        }
    }

    "ordinals" {
        forAll(table(
                headers("raw", "formatted"),
                row(1, "1st"),
                row(2, "2nd"),
                row(3, "3rd"),
                row(4, "4th"),
                row(5, "5th"),
                row(6, "6th"),
                row(7, "7th"),
                row(8, "8th"),
                row(9, "9th"),
                row(0, "0th")
        )) { raw, formatted ->
            getOrdinalDate(raw) shouldEqual formatted
        }
    }

    "gets previous date object given a date" {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2017)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        val date = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 21)
        val newDate = cal.time
        getPreviousDate(date) shouldEqual newDate
    }

    "gets previous date object given a date" {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2017)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 22)
        val date = cal.time
        cal.set(Calendar.DAY_OF_MONTH, 23)
        val newDate = cal.time
        getNextDate(date) shouldEqual newDate
    }

})
