package repositories

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import entities.Curso

object Cursos : Table("cursos") {
    val id = integer("id").autoIncrement() // Primary key
    val codigo = varchar("codigo", 10) // Primary key
    val nombre = varchar("nombre", 100)
    val creditos = integer("creditos")
    val horasSemanales = integer("horas_semanales")

    override val primaryKey = PrimaryKey(id)
}

// Extension function to map ResultRow to Curso object
fun Cursos.toCurso(row: ResultRow): Curso = Curso(
    id = row[id],
    codigo = row[codigo],
    nombre = row[nombre],
    creditos = row[creditos],
    horasSemanales = row[horasSemanales]
)
