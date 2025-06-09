package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import entities.Profesor
import services.ProfesorService

fun Route.profesorRoutes(profesorService: ProfesorService) {

    route("/profesores") {
        get {
            val profesores = profesorService.getAllProfesores()
            call.respond(profesores) // Ktor handles empty list serialization
        }

        post {
            try {
                val profesor = call.receive<Profesor>() // id in this object is ignored by service
                if (profesor.cedula.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Cedula cannot be null or empty for a new profesor")
                    return@post
                }
                // Check if profesor with this cedula already exists
                if (profesorService.getProfesorByCedula(profesor.cedula) != null) {
                    call.respond(HttpStatusCode.Conflict, "Profesor with cedula ${profesor.cedula} already exists.")
                    return@post
                }
                val createdProfesor = profesorService.addProfesor(profesor)
                call.respond(HttpStatusCode.Created, createdProfesor)
            } catch (e: IllegalArgumentException) { // From service (e.g. cedula constraint) or manual check
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid input for profesor")
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: Exception) {
                application.log.error("Failed to create profesor", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create profesor: ${e.localizedMessage}")
            }
        }

        // GET /profesores/cedula/{cedula} - Get a profesor by cedula
        get("/cedula/{cedula}") {
            val cedula = call.parameters["cedula"]
            if (cedula == null) {
                call.respond(HttpStatusCode.BadRequest, "Cedula parameter is missing")
                return@get
            }
            val profesor = profesorService.getProfesorByCedula(cedula)
            if (profesor != null) {
                call.respond(profesor)
            } else {
                call.respond(HttpStatusCode.NotFound, "Profesor with cedula $cedula not found")
            }
        }

        // Route for operations on a specific profesor by ID
        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Profesor ID format")
                    return@get
                }
                val profesor = profesorService.getProfesorById(id)
                if (profesor != null) {
                    call.respond(profesor)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Profesor with id $id not found")
                }
            }

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Profesor ID format")
                    return@put
                }
                try {
                    val profesorToUpdate = call.receive<Profesor>()
                    
                    if (profesorService.getProfesorById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Profesor with id $id not found for update.")
                        return@put
                    }

                    // Optional: If cedula can be updated and needs conflict check, service should handle or do it here.
                    // The service's updateProfesor has a commented section for this.
                    // If service throws IllegalArgumentException for cedula conflict, it will be caught below.

                    val updated = profesorService.updateProfesor(id, profesorToUpdate)
                    if (updated) {
                        val updatedProfesor = profesorService.getProfesorById(id)
                        call.respond(HttpStatusCode.OK, updatedProfesor ?: "Profesor updated but could not be retrieved.")
                    } else {
                        // This case implies the profesor was found (checked above) but the update operation itself failed
                        // or made no changes. The service returns boolean, so it's hard to distinguish without more info.
                        call.respond(HttpStatusCode.InternalServerError, "Profesor with id $id found but update failed unexpectedly.")
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: IllegalArgumentException) { // From service (e.g. cedula conflict during update)
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: Exception) {
                    application.log.error("Failed to update profesor $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update profesor: ${e.localizedMessage}")
                }
            }

            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Profesor ID format")
                    return@delete
                }
                if (profesorService.getProfesorById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Profesor with id $id not found for deletion.")
                    return@delete
                }
                val deleted = profesorService.deleteProfesor(id)
                if (deleted) {
                    call.respondText("Profesor with id $id deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Profesor with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }
}

fun Application.configureProfesorRouting(profesorService: ProfesorService) {
    routing {
        profesorRoutes(profesorService)
    }
}
