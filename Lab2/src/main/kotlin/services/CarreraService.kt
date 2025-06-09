package services

import entities.Carrera
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.Carreras
import repositories.DatabaseFactory.dbQuery


class CarreraService {

    suspend fun getAllCarreras(): List<Carrera> = dbQuery {
        Carreras.selectAll().map { toCarrera(it) }
    }

    // Keep getCarreraByCodigo for uniqueness checks or specific lookups
    suspend fun getCarreraByCodigo(codigo: String): Carrera? = dbQuery {
        Carreras.select { Carreras.codigo eq codigo }
            .mapNotNull { toCarrera(it) }
            .singleOrNull()
    }

    // New method to get Carrera by primary key 'id'
    suspend fun getCarreraById(id: Int): Carrera? = dbQuery {
        Carreras.select { Carreras.id eq id }
            .mapNotNull { toCarrera(it) }
            .singleOrNull()
    }

    suspend fun addCarrera(carrera: Carrera): Carrera {
        // Optional: Check if codigo already exists before insert
        // val existingCarrera = getCarreraByCodigo(carrera.codigo)
        // if (existingCarrera != null) {
        //     throw IllegalArgumentException("Carrera with codigo ${carrera.codigo} already exists.")
        // }
        return dbQuery {
            val newId = Carreras.insert {
                it[codigo] = carrera.codigo
                it[nombre] = carrera.nombre
                it[titulo] = carrera.titulo
            } get Carreras.id // Get the auto-generated ID

            // Fetch the newly created carrera by its ID
            Carreras.select { Carreras.id eq newId }
                .mapNotNull { toCarrera(it) }
                .singleOrNull() ?: throw IllegalStateException("Failed to retrieve carrera after insert with id $newId")
        }
    }

    suspend fun updateCarrera(id: Int, carreraUpdate: Carrera): Boolean {
        // Optional: If codigo is being updated, ensure the new codigo isn't already taken
        // val currentCarrera = getCarreraById(id)
        // if (currentCarrera != null && carreraUpdate.codigo != currentCarrera.codigo) {
        //     val existingCarreraWithNewCodigo = getCarreraByCodigo(carreraUpdate.codigo)
        //     if (existingCarreraWithNewCodigo != null && existingCarreraWithNewCodigo.id != id) {
        //         throw IllegalArgumentException("Another carrera with codigo ${carreraUpdate.codigo} already exists.")
        //     }
        // }
        return dbQuery {
            Carreras.update({ Carreras.id eq id }) {
                it[codigo] = carreraUpdate.codigo // Allow codigo update if necessary
                it[nombre] = carreraUpdate.nombre
                it[titulo] = carreraUpdate.titulo
            } > 0
        }
    }

    suspend fun deleteCarrera(id: Int): Boolean {
        return dbQuery {
            Carreras.deleteWhere { Carreras.id eq id } > 0
        }
    }

    private fun toCarrera(row: ResultRow): Carrera =
        Carrera(
            id = row[Carreras.id],
            codigo = row[Carreras.codigo],
            nombre = row[Carreras.nombre],
            titulo = row[Carreras.titulo]
        )
}
