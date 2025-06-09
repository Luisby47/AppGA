package controllers

import io.ktor.http.*
import io.ktor.server.application.* 
import io.ktor.server.request.*     
import io.ktor.server.response.*    
import io.ktor.server.routing.*     
import services.MatriculaService
import entities.Matricula

fun Route.configureMatriculaRouting(matriculaService: MatriculaService) {

    route("/matriculas") {
        // Get all matriculas
        get {
            val matriculas = matriculaService.getAllMatriculas()
            call.respond(matriculas)
        }

        // Create a new matricula
        post {
            // Assumes the client sends a Matricula object with alumnoId (Int) instead of alumnoCedula (String)
            // And id is null or not present, as it's auto-generated.
            val matricula = call.receive<Matricula>() 
            try {
                val createdMatricula = matriculaService.createMatricula(matricula)
                call.respond(HttpStatusCode.Created, createdMatricula)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Failed to create matricula. Invalid input or referenced Alumno/Grupo does not exist.")
            } catch (e: Exception) {
                application.log.error("Failed to create matricula", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create matricula due to an internal error: ${e.localizedMessage}")
            }
        }

        // Routes for specific matricula by ID
        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Matricula ID format. Must be an integer.")
                    return@get
                }
                val matricula = matriculaService.getMatriculaById(id)
                if (matricula != null) {
                    call.respond(matricula)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Matricula with id $id not found")
                }
            }

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Matricula ID format. Must be an integer.")
                    return@put
                }
                // Assumes the client sends a Matricula object with alumnoId (Int) if it's part of the update.
                val matriculaUpdate = call.receive<Matricula>()
                
                try {
                    val updated = matriculaService.updateMatricula(id, matriculaUpdate)
                    if (updated) {
                        val updatedMatricula = matriculaService.getMatriculaById(id)
                        if (updatedMatricula != null) {
                            call.respond(HttpStatusCode.OK, updatedMatricula)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Matricula updated but could not be retrieved.") // Should not happen
                        }
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Matricula with id $id not found or update failed (e.g., no actual change or data integrity issue).")
                    }
                } catch (e: IllegalArgumentException) {
                     call.respond(HttpStatusCode.BadRequest, e.message ?: "Update failed due to invalid input or referenced Alumno/Grupo does not exist.")
                } catch (e: Exception) {
                    application.log.error("Failed to update matricula $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update matricula due to an internal error: ${e.localizedMessage}")
                }
            }

            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Matricula ID format. Must be an integer.")
                    return@delete
                }
                val deleted = matriculaService.deleteMatricula(id)
                if (deleted) {
                    call.respondText("Matricula with id $id deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Matricula with id $id not found or delete failed")
                }
            }
        }

        // Get matriculas by alumno id
        get("/alumno/{alumnoId}") { // Changed from alumnoCedula to alumnoId
            val alumnoId = call.parameters["alumnoId"]?.toIntOrNull() // Parse as Int
            if (alumnoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Alumno ID must be an integer.")
                return@get
            }
            try {
                val matriculas = matriculaService.getMatriculasByAlumnoId(alumnoId)
                call.respond(matriculas) // Return the list of matriculas (may be empty)
            } catch (e: Exception) {
                application.log.error("Failed to retrieve matriculas for alumno $alumnoId", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve matriculas: ${e.localizedMessage}")
            }
        }

        // Get alumnos by grupo id
        get("/grupo/{grupoId}/alumnos") { 
            val grupoId = call.parameters["grupoId"]?.toIntOrNull() // Ensure grupoId is Int
            if (grupoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Grupo ID must be an integer.")
                return@get
            }
            val alumnos = matriculaService.getAlumnosByGrupo(grupoId)
            call.respond(alumnos)
        }
    }
}

fun Application.configureMatriculaRoutes(matriculaService: MatriculaService) {
    routing {
        configureMatriculaRouting(matriculaService)
    }
}
