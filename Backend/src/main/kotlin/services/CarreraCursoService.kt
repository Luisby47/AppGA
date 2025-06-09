package services

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import repositories.*
import entities.Curso
import entities.Carrera

class CarreraCursoService {

    @kotlinx.serialization.Serializable
    data class CarreraCursoAssociation(val carreraId: Int, val cursoId: Int)

    fun addCursoToCarrera(carreraIdInput: Int, cursoIdInput: Int): CarreraCursoAssociation? {
        return transaction {
            try {
                val existing = CarreraCursos.select {
                    (CarreraCursos.carreraId eq carreraIdInput) and (CarreraCursos.cursoId eq cursoIdInput)
                }.count() > 0
                if (existing) {
                    return@transaction CarreraCursoAssociation(carreraIdInput, cursoIdInput)
                }

                CarreraCursos.insert {
                    it[carreraId] = carreraIdInput
                    it[cursoId] = cursoIdInput
                }
                CarreraCursoAssociation(carreraIdInput, cursoIdInput)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun removeCursoFromCarrera(carreraIdInput: Int, cursoIdInput: Int): Boolean {
        return transaction {
            CarreraCursos.deleteWhere {
                (CarreraCursos.carreraId eq carreraIdInput) and (CarreraCursos.cursoId eq cursoIdInput)
            } > 0
        }
    }

    fun getCursosByCarreraId(carreraIdInput: Int): List<Curso> {
        return transaction {
            CarreraCursos.join(Cursos, JoinType.INNER, additionalConstraint = { CarreraCursos.cursoId eq Cursos.id })
                .select { CarreraCursos.carreraId eq carreraIdInput }
                .map { Cursos.toCurso(it) }
        }
    }

    fun getCarrerasByCursoId(cursoIdInput: Int): List<Carrera> {
        return transaction {
            CarreraCursos.join(Carreras, JoinType.INNER, additionalConstraint = { CarreraCursos.carreraId eq Carreras.id })
                .select { CarreraCursos.cursoId eq cursoIdInput }
                .map { Carreras.toCarrera(it) }
        }
    }

    fun getAllAssociations(): List<CarreraCursoAssociation> {
        return transaction {
            CarreraCursos.selectAll().map { 
                CarreraCursoAssociation(
                    it[CarreraCursos.carreraId],
                    it[CarreraCursos.cursoId]
                )
            }
        }
    }
}

// Reminder: Ensure Cursos.kt and Carreras.kt have updated toCurso/toCarrera helper methods
// that correctly map ResultRow to Curso/Carrera entities, including their 'id' fields.
// Example (in Cursos.kt):
// fun Cursos.toCurso(row: ResultRow): Curso = Curso(
//     id = row[Cursos.id],
//     codigo = row[Cursos.codigo],
//     nombre = row[Cursos.nombre],
//     creditos = row[Cursos.creditos],
//     horasSemanales = row[Cursos.horasSemanales]
// )
// Similar for Carreras.toCarrera in Carreras.kt
