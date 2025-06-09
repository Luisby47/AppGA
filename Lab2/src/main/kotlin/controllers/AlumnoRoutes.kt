package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import entities.Alumno
import services.AlumnoService

fun Route.alumnoRoutes(alumnoService: AlumnoService) {

    route("/alumnos") {
        // GET /alumnos - Get all alumnos
        get {
            val alumnos = alumnoService.getAllAlumnos()
            // Respond with the list, even if empty, Ktor handles empty list serialization correctly.
            call.respond(alumnos)
        }

        // POST /alumnos - Create a new alumno
        post {
            try {
                val alumno = call.receive<Alumno>() // id in this alumno object is ignored by service
                // Basic validation: check if cedula is provided
                if (alumno.cedula.isBlank()) {
                     call.respond(HttpStatusCode.BadRequest, "Cedula cannot be empty")
                     return@post
                }
                // Check if alumno with this cedula already exists (using the dedicated service method)
                if (alumnoService.getAlumnoByCedula(alumno.cedula) != null) {
                    call.respond(HttpStatusCode.Conflict, "Alumno with cedula ${alumno.cedula} already exists.")
                    return@post
                }
                val createdAlumno = alumnoService.addAlumno(alumno)
                call.respond(HttpStatusCode.Created, createdAlumno)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: IllegalArgumentException) { // Catch specific exception from service
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } catch (e: Exception) {
                application.log.error("Failed to create alumno", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create alumno: ${e.localizedMessage}")
            }
        }

        // GET /alumnos/cedula/{cedula} - Get an alumno by cedula
        get("/cedula/{cedula}") {
            val cedula = call.parameters["cedula"]
            if (cedula == null) {
                call.respond(HttpStatusCode.BadRequest, "Cedula parameter is missing")
                return@get
            }
            val alumno = alumnoService.getAlumnoByCedula(cedula)
            if (alumno != null) {
                call.respond(alumno)
            } else {
                call.respond(HttpStatusCode.NotFound, "Alumno with cedula $cedula not found")
            }
        }

        // Route for operations on a specific alumno by ID
        route("/{id}") {
            // GET /alumnos/{id} - Get an alumno by ID
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Alumno ID format")
                    return@get
                }
                val alumno = alumnoService.getAlumnoById(id)
                if (alumno != null) {
                    call.respond(alumno)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Alumno with id $id not found")
                }
            }

            // PUT /alumnos/{id} - Update an alumno by ID
            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Alumno ID format")
                    return@put
                }
                try {
                    val alumnoToUpdate = call.receive<Alumno>()
                    // Optional: Validate that alumnoToUpdate.id (if present and not default) matches path id.
                    // The service uses the path 'id' as the source of truth.

                    // Check if the alumno to update actually exists
                    if (alumnoService.getAlumnoById(id) == null) {
                         call.respond(HttpStatusCode.NotFound, "Alumno with id $id not found for update.")
                         return@put
                    }

                    // Optional: If cedula can be updated, check for conflicts
                    // val currentAlumno = alumnoService.getAlumnoById(id)!! // We know it exists from above check
                    // if (alumnoToUpdate.cedula != currentAlumno.cedula) {
                    //     if (alumnoService.getAlumnoByCedula(alumnoToUpdate.cedula) != null) {
                    //         call.respond(HttpStatusCode.Conflict, "Another alumno with cedula ${alumnoToUpdate.cedula} already exists.")
                    //         return@put
                    //     }
                    // }

                    val updated = alumnoService.updateAlumno(id, alumnoToUpdate)
                    if (updated) {
                        val updatedAlumno = alumnoService.getAlumnoById(id) // Fetch the updated alumno
                        call.respond(HttpStatusCode.OK, updatedAlumno ?: "Alumno updated but could not be retrieved.")
                    } else {
                        // This might indicate a concurrent modification or other issue if the alumno existed before.
                        call.respond(HttpStatusCode.InternalServerError, "Alumno with id $id found but update failed unexpectedly.")
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: IllegalArgumentException) { // Catch specific exception from service (e.g. cedula conflict)
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: Exception) {
                    application.log.error("Failed to update alumno $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update alumno: ${e.localizedMessage}")
                }
            }

            // DELETE /alumnos/{id} - Delete an alumno by ID
            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Alumno ID format")
                    return@delete
                }
                if (alumnoService.getAlumnoById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Alumno with id $id not found for deletion.")
                    return@delete
                }
                val deleted = alumnoService.deleteAlumno(id)
                if (deleted) {
                    call.respondText("Alumno with id $id deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Alumno with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }
}

// Helper function to configure routing, typically called from Application.kt or a main routing setup file.
// Ensure AlumnoService is properly injected or available here.
fun Application.configureAlumnoRouting(alumnoService: AlumnoService) {
    routing {
        alumnoRoutes(alumnoService) // Assuming alumnoRoutes is an extension on Route
    }
}
