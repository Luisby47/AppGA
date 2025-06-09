package services

import entities.Grupo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.DatabaseFactory.dbQuery
import repositories.Grupos
import repositories.Cursos


class GrupoService {

    suspend fun getAllGrupos(): List<Grupo> = dbQuery {
        Grupos.selectAll().map { toGrupo(it) }
    }

    suspend fun getGrupoById(id: Int): Grupo? = dbQuery {
        Grupos.select { Grupos.id eq id }
            .mapNotNull { toGrupo(it) }
            .singleOrNull()
    }

    // New method to get Grupo by its natural composite key
    suspend fun getGrupoByCompositeKey(anio: Int, numeroCiclo: String, codigoCurso: String, numeroGrupo: Int): Grupo? = dbQuery {
        Grupos.select {
            (Grupos.anio eq anio) and
            (Grupos.numeroCiclo eq numeroCiclo) and
            (Grupos.codigoCurso eq codigoCurso) and
            (Grupos.numeroGrupo eq numeroGrupo)
        }
        .mapNotNull { toGrupo(it) }
        .singleOrNull()
    }

    suspend fun addGrupo(grupo: Grupo): Grupo {
        // Check for uniqueness based on the natural composite key
        if (getGrupoByCompositeKey(grupo.anio, grupo.numeroCiclo, grupo.codigoCurso, grupo.numeroGrupo) != null) {
            throw IllegalArgumentException("Grupo with anio ${grupo.anio}, ciclo ${grupo.numeroCiclo}, curso ${grupo.codigoCurso}, and grupo #${grupo.numeroGrupo} already exists.")
        }

        val generatedId = dbQuery {
            Grupos.insert {
                it[anio] = grupo.anio
                it[numeroCiclo] = grupo.numeroCiclo
                it[codigoCurso] = grupo.codigoCurso
                it[numeroGrupo] = grupo.numeroGrupo
                it[horario] = grupo.horario
                it[cedulaProfesor] = grupo.cedulaProfesor
            } get Grupos.id
        }
        return getGrupoById(generatedId) ?: throw IllegalStateException("Failed to retrieve grupo after insert with id $generatedId")
    }

    suspend fun updateGrupo(idGrupo: Int, grupoUpdate: Grupo): Boolean {
        // Check if the new composite key conflicts with an existing grupo (other than the one being updated)
        val existingGrupoWithNewKey = getGrupoByCompositeKey(grupoUpdate.anio, grupoUpdate.numeroCiclo, grupoUpdate.codigoCurso, grupoUpdate.numeroGrupo)
        if (existingGrupoWithNewKey != null && existingGrupoWithNewKey.id != idGrupo) {
            throw IllegalArgumentException("Another grupo with anio ${grupoUpdate.anio}, ciclo ${grupoUpdate.numeroCiclo}, curso ${grupoUpdate.codigoCurso}, and grupo #${grupoUpdate.numeroGrupo} already exists.")
        }

        return dbQuery {
            Grupos.update({ Grupos.id eq idGrupo }) {
                it[anio] = grupoUpdate.anio
                it[numeroCiclo] = grupoUpdate.numeroCiclo
                it[codigoCurso] = grupoUpdate.codigoCurso
                it[numeroGrupo] = grupoUpdate.numeroGrupo
                it[horario] = grupoUpdate.horario
                it[cedulaProfesor] = grupoUpdate.cedulaProfesor
            } > 0
        }
    }

    suspend fun deleteGrupo(idGrupo: Int): Boolean {
        return dbQuery {
            Grupos.deleteWhere { Grupos.id eq idGrupo } > 0
        }
    }

    // Get grupos by curso id
    suspend fun getGruposByCursoId(cursoId: Int): List<Grupo> = dbQuery {
        val curso = Cursos.select { Cursos.id eq cursoId }.singleOrNull()
        if (curso != null) {
            val codigoCurso = curso[Cursos.codigo]
            Grupos.select { Grupos.codigoCurso eq codigoCurso }.map { toGrupo(it) }
        } else {
            emptyList()
        }
    }

    // Get grupos by profesor cedula
    suspend fun getGruposByProfesorCedula(cedulaProfesor: String): List<Grupo> = dbQuery {
        Grupos.select { Grupos.cedulaProfesor eq cedulaProfesor }.map { toGrupo(it) }
    }

    private fun toGrupo(row: ResultRow): Grupo =
        Grupo(
            id = row[Grupos.id],
            anio = row[Grupos.anio],
            numeroCiclo = row[Grupos.numeroCiclo],
            codigoCurso = row[Grupos.codigoCurso],
            numeroGrupo = row[Grupos.numeroGrupo],
            horario = row[Grupos.horario],
            cedulaProfesor = row[Grupos.cedulaProfesor]
        )
}
