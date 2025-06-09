package services

import entities.Curso
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.Cursos
import repositories.DatabaseFactory.dbQuery


class CursoService {

    suspend fun getAllCursos(): List<Curso> = dbQuery {
        Cursos.selectAll().map { toCurso(it) }
    }

    // Keep getCursoByCodigo for uniqueness checks or specific lookups
    suspend fun getCursoByCodigo(codigo: String): Curso? = dbQuery {
        Cursos.select { Cursos.codigo eq codigo }
            .mapNotNull { toCurso(it) }
            .singleOrNull()
    }

    // New method to get Curso by primary key 'id'
    suspend fun getCursoById(id: Int): Curso? = dbQuery {
        Cursos.select { Cursos.id eq id }
            .mapNotNull { toCurso(it) }
            .singleOrNull()
    }

    suspend fun addCurso(curso: Curso): Curso {
        // Optional: Check if codigo already exists before insert
        // val existingCurso = getCursoByCodigo(curso.codigo)
        // if (existingCurso != null) {
        //     throw IllegalArgumentException("Curso with codigo ${curso.codigo} already exists.")
        // }
        return dbQuery {
            val newId = Cursos.insert {
                it[codigo] = curso.codigo
                it[nombre] = curso.nombre
                it[creditos] = curso.creditos
                it[horasSemanales] = curso.horasSemanales
            } get Cursos.id // Get the auto-generated ID

            // Fetch the newly created curso by its ID
            Cursos.select { Cursos.id eq newId }
                .mapNotNull { toCurso(it) }
                .singleOrNull() ?: throw IllegalStateException("Failed to retrieve curso after insert with id $newId")
        }
    }

    suspend fun updateCurso(id: Int, cursoUpdate: Curso): Boolean {
        // Optional: If codigo is being updated, ensure the new codigo isn't already taken
        // val currentCurso = getCursoById(id)
        // if (currentCurso != null && cursoUpdate.codigo != currentCurso.codigo) {
        //     val existingCursoWithNewCodigo = getCursoByCodigo(cursoUpdate.codigo)
        //     if (existingCursoWithNewCodigo != null && existingCursoWithNewCodigo.id != id) {
        //         throw IllegalArgumentException("Another curso with codigo ${cursoUpdate.codigo} already exists.")
        //     }
        // }
        return dbQuery {
            Cursos.update({ Cursos.id eq id }) {
                it[codigo] = cursoUpdate.codigo // Allow codigo update if necessary
                it[nombre] = cursoUpdate.nombre
                it[creditos] = cursoUpdate.creditos
                it[horasSemanales] = cursoUpdate.horasSemanales
            } > 0
        }
    }

    suspend fun deleteCurso(id: Int): Boolean {
        return dbQuery {
            Cursos.deleteWhere { Cursos.id eq id } > 0
        }
    }

    private fun toCurso(row: ResultRow): Curso =
        Curso(
            id = row[Cursos.id],
            codigo = row[Cursos.codigo],
            nombre = row[Cursos.nombre],
            creditos = row[Cursos.creditos],
            horasSemanales = row[Cursos.horasSemanales]
        )
}
