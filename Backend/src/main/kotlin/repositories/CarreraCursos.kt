package repositories

import org.jetbrains.exposed.sql.Table

object CarreraCursos : Table("carrera_cursos") {

    val carreraId = integer("carrera_id") references Carreras.id
    val cursoId = integer("curso_id") references Cursos.id
    // No additional fields are strictly necessary for a simple join table,
    // but you could add fields like 'semestre_sugerido' (suggested semester) if needed.

    override val primaryKey = PrimaryKey(carreraId, cursoId, name = "PK_CARRERA_CURSO_ID")
}
