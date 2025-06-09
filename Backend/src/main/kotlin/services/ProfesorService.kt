package services

import entities.Profesor
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.DatabaseFactory.dbQuery
import repositories.Profesores


class ProfesorService {

    suspend fun getAllProfesores(): List<Profesor> = dbQuery {
        Profesores.selectAll().map { toProfesor(it) }
    }

    // Keep getProfesorByCedula for uniqueness checks or specific lookups
    suspend fun getProfesorByCedula(cedula: String): Profesor? = dbQuery {
        Profesores.select { Profesores.cedula eq cedula }
            .mapNotNull { toProfesor(it) }
            .singleOrNull()
    }

    // New method to get Profesor by primary key 'id'
    suspend fun getProfesorById(id: Int): Profesor? = dbQuery {
        Profesores.select { Profesores.id eq id }
            .mapNotNull { toProfesor(it) }
            .singleOrNull()
    }

    suspend fun addProfesor(profesor: Profesor): Profesor {
        val profesorCedula = profesor.cedula ?: throw IllegalArgumentException("Cedula cannot be null for a new profesor")
        // Optional: Check if cedula already exists before insert
        // val existingProfesor = getProfesorByCedula(profesorCedula)
        // if (existingProfesor != null) {
        //     throw IllegalArgumentException("Profesor with cedula $profesorCedula already exists.")
        // }
        return dbQuery {
            val newId = Profesores.insert {
                it[cedula] = profesorCedula
                it[nombre] = profesor.nombre
                it[telefono] = profesor.telefono
                it[email] = profesor.email
            } get Profesores.id // Get the auto-generated ID

            // Fetch the newly created profesor by its ID
            Profesores.select { Profesores.id eq newId }
                .mapNotNull { toProfesor(it) }
                .singleOrNull() ?: throw IllegalStateException("Failed to retrieve profesor after insert with id $newId")
        }
    }

    suspend fun updateProfesor(id: Int, profesorUpdate: Profesor): Boolean {
        // Optional: If cedula is being updated, ensure the new cedula isn't already taken by another profesor.
        // val currentProfesor = getProfesorById(id)
        // if (currentProfesor != null && profesorUpdate.cedula != null && profesorUpdate.cedula != currentProfesor.cedula) {
        //     val existingProfesorWithNewCedula = getProfesorByCedula(profesorUpdate.cedula!!)
        //     if (existingProfesorWithNewCedula != null && existingProfesorWithNewCedula.id != id) {
        //         throw IllegalArgumentException("Another profesor with cedula ${profesorUpdate.cedula} already exists.")
        //     }
        // }
        return dbQuery {
            Profesores.update({ Profesores.id eq id }) {
                profesorUpdate.cedula?.let { newCedula -> it[Profesores.cedula] = newCedula } // Allow cedula update
                profesorUpdate.nombre?.let { nonNullNombre -> it[Profesores.nombre] = nonNullNombre }
                profesorUpdate.telefono?.let { nonNullTelefono -> it[Profesores.telefono] = nonNullTelefono }
                profesorUpdate.email?.let { nonNullEmail -> it[Profesores.email] = nonNullEmail }
            } > 0
        }
    }

    suspend fun deleteProfesor(id: Int): Boolean {
        return dbQuery {
            Profesores.deleteWhere { Profesores.id eq id } > 0
        }
    }

    private fun toProfesor(row: ResultRow): Profesor =
        Profesor(
            id = row[Profesores.id],
            cedula = row[Profesores.cedula],
            nombre = row[Profesores.nombre],
            telefono = row[Profesores.telefono],
            email = row[Profesores.email]
        )
}
