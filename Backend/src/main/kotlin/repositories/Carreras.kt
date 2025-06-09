package repositories

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import entities.Carrera

object Carreras : Table("carreras") {
    val id = integer("id").autoIncrement() // Primary key
    val codigo = varchar("codigo", 10) // Primary key
    val nombre = varchar("nombre", 100)
    val titulo = varchar("titulo", 150)

    override val primaryKey = PrimaryKey(id)
}

// Extension function to map ResultRow to Carrera object
fun Carreras.toCarrera(row: ResultRow): Carrera = Carrera(
    id = row[id],
    codigo = row[codigo],
    nombre = row[nombre],
    titulo = row[titulo]
)
