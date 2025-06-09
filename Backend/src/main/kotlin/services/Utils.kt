package services

import java.text.SimpleDateFormat
import java.sql.Date


class Utils {

    companion object {
        // Función para convertir un String a java.sql.Date
        fun stringToSqlDate(dateString: String): Date? {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd")
                val parsedDate = format.parse(dateString)
                // Convertir java.util.Date a java.sql.Date
                Date(parsedDate.time)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}