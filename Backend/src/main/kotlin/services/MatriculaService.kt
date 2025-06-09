package services

import entities.Matricula
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import repositories.*
import repositories.DatabaseFactory.dbQuery
import entities.Alumno
import entities.Grupo



class MatriculaService {

    suspend fun createMatricula(matricula: Matricula): Matricula {
        // Ensure matricula.id is null for creation, as it's auto-generated
        val matriculaToCreate = matricula.copy(id = null)
        return DatabaseFactory.dbQuery {
            // Check if Alumno and Grupo exist
            val alumnoExists = Alumnos.select { Alumnos.id eq matriculaToCreate.alumnoId }.count() > 0
            val grupoExists = Grupos.select { Grupos.id eq matriculaToCreate.grupoId }.count() > 0

            if (alumnoExists && grupoExists) {
                val newId = Matriculas.insert {
                    it[alumnoId] = matriculaToCreate.alumnoId 
                    it[grupoId] = matriculaToCreate.grupoId
                    it[nota] = matriculaToCreate.nota
                } get Matriculas.id 

                // Fetch the newly created matricula by its ID
                Matriculas.select { Matriculas.id eq newId }
                    .mapNotNull { Matriculas.toMatricula(it) }
                    .singleOrNull() ?: throw IllegalStateException("Failed to retrieve matricula after insert with id $newId")
            } else {
                throw IllegalArgumentException("Alumno with id ${matriculaToCreate.alumnoId} or Grupo with id ${matriculaToCreate.grupoId} does not exist.")
            }
        }
    }

    suspend fun getMatriculaById(id: Int): Matricula? {
        return DatabaseFactory.dbQuery {
            Matriculas.select { Matriculas.id eq id }
                .mapNotNull { Matriculas.toMatricula(it) }
                .singleOrNull()
        }
    }

    suspend fun getAllMatriculas(): List<Matricula> {
        return DatabaseFactory.dbQuery {
            Matriculas.selectAll().map { Matriculas.toMatricula(it) }
        }
    }

    suspend fun getMatriculasByAlumnoId(alumnoIdParam: Int): List<Matricula> { 
        return DatabaseFactory.dbQuery {
            Matriculas.select { Matriculas.alumnoId eq alumnoIdParam } 
                .map { Matriculas.toMatricula(it) }
        }
    }

    suspend fun getAlumnosByGrupo(grupoIdParam: Int): List<Alumno> {
        return DatabaseFactory.dbQuery {
            Matriculas.join(Alumnos, JoinType.INNER, additionalConstraint = { Matriculas.alumnoId eq Alumnos.id }) 
                .select { Matriculas.grupoId eq grupoIdParam }
                .map { Alumnos.toAlumno(it) } 
        }
    }

    suspend fun updateMatricula(id: Int, matriculaUpdate: Matricula): Boolean {
        return DatabaseFactory.dbQuery {
            Matriculas.update({ Matriculas.id eq id }) {
                it[alumnoId] = matriculaUpdate.alumnoId 
                it[grupoId] = matriculaUpdate.grupoId
                it[nota] = matriculaUpdate.nota
            } > 0
        }
    }

    suspend fun deleteMatricula(id: Int): Boolean {
        return DatabaseFactory.dbQuery {
            Matriculas.deleteWhere { Matriculas.id eq id } > 0
        }
    }
}

// Reminder: Ensure Matriculas.kt has toMatricula(row: ResultRow) and Alumnos.kt has toAlumno(row: ResultRow)
