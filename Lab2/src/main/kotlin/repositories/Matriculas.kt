package repositories

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import entities.Matricula

object Matriculas : Table("matriculas") {
    val id = integer("id").autoIncrement() // Primary key
    val alumnoId = integer("alumno_id") references Alumnos.id
    val grupoId = integer("grupo_id") references Grupos.id
    val nota = integer("nota").nullable() // Grade, can be null initially

    override val primaryKey = PrimaryKey(id, name = "PK_MATRICULA_ID")

    // Foreign key constraints (already defined by 'references' but can be more explicit if needed)
    // init {
    //     foreignKey(
    //         alumnoId to Alumnos.id,
    //         name = "fk_matriculas_alumno",
    //         onDelete = ReferenceOption.CASCADE, // If an Alumno is deleted, their Matriculas are also deleted
    //         onUpdate = ReferenceOption.CASCADE
    //     )
    //     foreignKey(
    //         grupoId to Grupos.id,
    //         name = "fk_matriculas_grupo",
    //         onDelete = ReferenceOption.CASCADE, // If a Grupo is deleted, Matriculas for that group are also deleted
    //         onUpdate = ReferenceOption.CASCADE
    //     )
    // }
}

// Extension function to map ResultRow to Matricula object
fun Matriculas.toMatricula(row: ResultRow): Matricula = Matricula(
    id = row[id],
    alumnoId = row[alumnoId],
    grupoId = row[grupoId],
    nota = row[nota]
)
