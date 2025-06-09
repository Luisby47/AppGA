package repositories

import org.jetbrains.exposed.sql.Table

object Ciclos : Table("ciclos") {
    val id = integer("id").autoIncrement()
    val anio = integer("anio") // Non-nullable, part of unique constraint
    val numero = varchar("numero", 10) // Non-nullable, part of unique constraint
    val fechaInicio = varchar("fecha_inicio", 10).nullable() // Assuming "YYYY-MM-DD" format
    val fechaFin = varchar("fecha_fin", 10).nullable()   // Assuming "YYYY-MM-DD" format
    val activo = bool("activo").default(false)

    override val primaryKey = PrimaryKey(id) // Set 'id' as the primary key
    init {
        uniqueIndex("uq_ciclo_anio_numero", anio, numero) // Ensure anio and numero combination is unique
    }
}
