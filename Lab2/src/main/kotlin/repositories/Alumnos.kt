package repositories

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import entities.Alumno


object Alumnos : Table("alumnos") { // Table name in the database will be "alumnos"
    val id = integer("id").autoIncrement() // Primary key
    val cedula = varchar("cedula", 20)
    val nombre = varchar("nombre", 100)
    val telefono = varchar("telefono", 15).nullable()
    val email = varchar("email", 100)
    val fechaNacimiento = varchar("fecha_nacimiento", 20) // Storing as String as per entity, consider date type. Increased length for ISO dates.
    val codigoCarrera = varchar("codigo_carrera", 10).nullable()

    override val primaryKey = PrimaryKey(id)
}

// Extension function to map ResultRow to Alumno object
fun Alumnos.toAlumno(row: ResultRow): Alumno = Alumno(
    id = row[id],
    cedula = row[cedula],
    nombre = row[nombre],
    telefono = row[telefono],
    email = row[email],
    fechaNacimiento = row[fechaNacimiento],
    codigoCarrera = row[codigoCarrera]
)
