package repositories

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object Grupos : Table("grupos") {
    val id = integer("id").autoIncrement() // Primary key
    val anio = integer("anio_ciclo")
    val numeroCiclo = varchar("numero_ciclo", 10)
    val codigoCurso = varchar("codigo_curso", 10)
    val numeroGrupo = integer("numero_grupo")
    val horario = varchar("horario", 100).nullable()
    val cedulaProfesor = varchar("cedula_profesor", 20).nullable()

    override val primaryKey = PrimaryKey(id)

    // Foreign key constraints and unique index
    init {
        uniqueIndex("uq_grupo_natural_key", anio, numeroCiclo, codigoCurso, numeroGrupo)

        foreignKey(
            anio to Ciclos.anio,
            numeroCiclo to Ciclos.numero,
            name = "fk_grupos_ciclo",
            onDelete = ReferenceOption.RESTRICT, // Example: Prevent deleting a Ciclo if Grupos reference it
            onUpdate = ReferenceOption.CASCADE
        )
        foreignKey(
            codigoCurso to Cursos.codigo,
            name = "fk_grupos_curso",
            onDelete = ReferenceOption.RESTRICT,
            onUpdate = ReferenceOption.CASCADE
        )
        foreignKey(
            cedulaProfesor to Profesores.cedula,
            name = "fk_grupos_profesor",
            onDelete = ReferenceOption.SET_NULL, // Example: If a Profesor is deleted, set cedulaProfesor to NULL
            onUpdate = ReferenceOption.CASCADE
        )
    }
}
