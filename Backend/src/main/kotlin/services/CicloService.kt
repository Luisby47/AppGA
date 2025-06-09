package services

import entities.Ciclo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.Ciclos
import repositories.DatabaseFactory.dbQuery


class CicloService {

    suspend fun getAllCiclos(): List<Ciclo> = dbQuery {
        Ciclos.selectAll().map { toCiclo(it) }
    }

    suspend fun getCicloByAnioAndNumero(anio: Int, numero: String): Ciclo? = dbQuery {
        Ciclos.select { (Ciclos.anio eq anio) and (Ciclos.numero eq numero) }
            .mapNotNull { toCiclo(it) }
            .singleOrNull()
    }

    suspend fun getCicloById(id: Int): Ciclo? = dbQuery {
        Ciclos.select { Ciclos.id eq id }
            .mapNotNull { toCiclo(it) }
            .singleOrNull()
    }
    
    suspend fun getActiveCiclo(): Ciclo? = dbQuery {
        Ciclos.select { Ciclos.activo eq true }
            .mapNotNull { toCiclo(it) }
            .singleOrNull()
    }

    suspend fun addCiclo(ciclo: Ciclo): Ciclo {
        val newId = dbQuery {
            Ciclos.insert {
                it[anio] = ciclo.anio
                it[numero] = ciclo.numero
                it[fechaInicio] = ciclo.fechaInicio
                it[fechaFin] = ciclo.fechaFin
                it[activo] = ciclo.activo
            } get Ciclos.id 
        }

        return dbQuery {
            Ciclos.select { Ciclos.id eq newId }
                .mapNotNull { toCiclo(it) }
                .singleOrNull() ?: throw IllegalStateException("Failed to retrieve ciclo after insert with id $newId")
        }
    }

    suspend fun updateCiclo(id: Int, cicloUpdate: Ciclo): Boolean {
        return dbQuery {
            Ciclos.update({ Ciclos.id eq id }) {
                it[anio] = cicloUpdate.anio
                it[numero] = cicloUpdate.numero
                it[fechaInicio] = cicloUpdate.fechaInicio
                it[fechaFin] = cicloUpdate.fechaFin
                it[activo] = cicloUpdate.activo
            } > 0
        }
    }

    suspend fun deleteCiclo(id: Int): Boolean {
        return dbQuery {
            Ciclos.deleteWhere { Ciclos.id eq id } > 0
        }
    }
    
    suspend fun setCicloActivo(id: Int): Boolean {
        return dbQuery {
            // First, check if the ciclo exists
            val cicloExists = Ciclos.select { Ciclos.id eq id }.count() > 0
            
            if (!cicloExists) {
                return@dbQuery false
            }
            
            // First set all ciclos to inactive
            Ciclos.update { it[activo] = false }
            
            // Then set the specified ciclo to active
            Ciclos.update({ Ciclos.id eq id }) {
                it[activo] = true
            } > 0
        }
    }

    private fun toCiclo(row: ResultRow): Ciclo =
        Ciclo(
            id = row[Ciclos.id],
            anio = row[Ciclos.anio],
            numero = row[Ciclos.numero],
            fechaInicio = row[Ciclos.fechaInicio],
            fechaFin = row[Ciclos.fechaFin],
            activo = row[Ciclos.activo]
        )
}
