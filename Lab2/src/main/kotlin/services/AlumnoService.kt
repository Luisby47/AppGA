package services

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.Alumnos
import repositories.DatabaseFactory.dbQuery
import entities.Alumno

class AlumnoService {

    suspend fun getAllAlumnos(): List<Alumno> = dbQuery {
        Alumnos.selectAll().map { toAlumno(it) }
    }

    // Keep getAlumnoByCedula for uniqueness checks or specific lookups
    suspend fun getAlumnoByCedula(cedula: String): Alumno? = dbQuery {
        Alumnos.select { Alumnos.cedula eq cedula }
            .mapNotNull { toAlumno(it) }
            .singleOrNull()
    }

    // New method to get Alumno by primary key 'id'
    suspend fun getAlumnoById(id: Int): Alumno? = dbQuery {
        Alumnos.select { Alumnos.id eq id }
            .mapNotNull { toAlumno(it) }
            .singleOrNull()
    }

    suspend fun addAlumno(alumno: Alumno): Alumno {
        // Optional: Check if cedula already exists before insert if it must be unique
        // val existingAlumno = getAlumnoByCedula(alumno.cedula)
        // if (existingAlumno != null) {
        //     throw IllegalArgumentException("Alumno with cedula ${alumno.cedula} already exists.")
        // }

        return dbQuery {
            val newId = Alumnos.insert {
                it[cedula] = alumno.cedula
                it[nombre] = alumno.nombre
                it[telefono] = alumno.telefono
                it[email] = alumno.email
                it[fechaNacimiento] = alumno.fechaNacimiento
                it[codigoCarrera] = alumno.codigoCarrera
            } get Alumnos.id // Get the auto-generated ID

            // Fetch the newly created alumno by its ID
            Alumnos.select { Alumnos.id eq newId }
                .mapNotNull { toAlumno(it) }
                .singleOrNull() ?: throw IllegalStateException("Failed to retrieve alumno after insert with id $newId")
        }
    }

    suspend fun updateAlumno(id: Int, alumnoUpdate: Alumno): Boolean {
        // Optional: If cedula is being updated, ensure the new cedula isn't already taken by another alumno
        // if (alumnoUpdate.cedula != getAlumnoById(id)?.cedula) { // Check if cedula is actually changing
        //     val existingAlumnoWithNewCedula = getAlumnoByCedula(alumnoUpdate.cedula)
        //     if (existingAlumnoWithNewCedula != null && existingAlumnoWithNewCedula.id != id) {
        //         throw IllegalArgumentException("Another alumno with cedula ${alumnoUpdate.cedula} already exists.")
        //     }
        // }
        return dbQuery {
            Alumnos.update({ Alumnos.id eq id }) {
                it[cedula] = alumnoUpdate.cedula // Allow cedula update if necessary
                it[nombre] = alumnoUpdate.nombre
                it[telefono] = alumnoUpdate.telefono
                it[email] = alumnoUpdate.email
                it[fechaNacimiento] = alumnoUpdate.fechaNacimiento
                it[codigoCarrera] = alumnoUpdate.codigoCarrera
            } > 0
        }
    }

    suspend fun deleteAlumno(id: Int): Boolean {
        return dbQuery {
            Alumnos.deleteWhere { Alumnos.id eq id } > 0
        }
    }

    private fun toAlumno(row: ResultRow): Alumno =
        Alumno(
            id = row[Alumnos.id],
            cedula = row[Alumnos.cedula],
            nombre = row[Alumnos.nombre],
            telefono = row[Alumnos.telefono],
            email = row[Alumnos.email],
            fechaNacimiento = row[Alumnos.fechaNacimiento],
            codigoCarrera = row[Alumnos.codigoCarrera]
        )
}
