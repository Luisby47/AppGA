package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import entities.Ciclo
import services.CicloService
import io.ktor.server.plugins.ContentTransformationException

fun Route.cicloRoutes(cicloService: CicloService) {

    route("/ciclos") {
        get {
            val ciclos = cicloService.getAllCiclos()
            call.respond(ciclos) // Ktor handles empty list serialization
        }
        
        // Get the active ciclo
        get("/activo") {
            try {
                val activeCiclo = cicloService.getActiveCiclo()
                if (activeCiclo != null) {
                    call.respond(activeCiclo)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No active ciclo found")
                }
            } catch (e: Exception) {
                application.log.error("Failed to get active ciclo", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to get active ciclo: ${e.localizedMessage}")
            }
        }

        post {
            try {
                val ciclo = call.receive<Ciclo>() // id in this object is ignored by service
                // anio and numero are non-nullable in Ciclo entity due to table definition
                // Service will handle unique constraint for (anio, numero) or throw an error.
                // Explicit check before calling service for better error message:
                if (cicloService.getCicloByAnioAndNumero(ciclo.anio, ciclo.numero) != null) {
                    call.respond(HttpStatusCode.Conflict, "Ciclo with anio ${ciclo.anio} and numero ${ciclo.numero} already exists.")
                    return@post
                }
                val createdCiclo = cicloService.addCiclo(ciclo)
                call.respond(HttpStatusCode.Created, createdCiclo)
            } catch (e: IllegalArgumentException) { // From service (e.g. if anio/numero conflict not caught above)
                 call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid input for Ciclo.")
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: Exception) {
                application.log.error("Failed to create ciclo", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create ciclo: ${e.localizedMessage}")
            }
        }

        // GET /ciclos/byAnioNumero/{anio}/{numero} - Get a ciclo by anio and numero
        get("/byAnioNumero/{anio}/{numero}") {
            val anio = call.parameters["anio"]?.toIntOrNull()
            val numero = call.parameters["numero"]
            
            if (anio == null || numero == null) {
                call.respond(HttpStatusCode.BadRequest, "Año (anio) and Número (numero) parameters are required and must be valid.")
                return@get
            }

            val ciclo = cicloService.getCicloByAnioAndNumero(anio, numero)
            if (ciclo != null) {
                call.respond(ciclo)
            } else {
                call.respond(HttpStatusCode.NotFound, "Ciclo with anio $anio and numero $numero not found.")
            }
        }

        // Route to set a specific ciclo as active
        get("/{id}/activo") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Ciclo ID format")
                    return@get
                }
                
                val success = cicloService.setCicloActivo(id)
                if (success) {
                    val activeCiclo = cicloService.getCicloById(id)
                    if (activeCiclo != null) {
                        call.respond(activeCiclo)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Ciclo set as active but could not be retrieved.")
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Ciclo with id $id not found")
                }
            } catch (e: Exception) {
                application.log.error("Failed to set ciclo as active", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to set ciclo as active: ${e.localizedMessage}")
            }
        }
        
        // Route for operations on a specific ciclo by ID
        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Ciclo ID format")
                    return@get
                }
                val ciclo = cicloService.getCicloById(id)
                if (ciclo != null) {
                    call.respond(ciclo)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Ciclo with id $id not found")
                }
            }

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Ciclo ID format")
                    return@put
                }

                try {
                    val cicloToUpdate = call.receive<Ciclo>()
                    
                    if (cicloService.getCicloById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Ciclo with id $id not found for update.")
                        return@put
                    }

                    // Service's updateCiclo allows updating anio, numero, etc.
                    // Service should handle uniqueness check for anio/numero if they are changed.
                    val updated = cicloService.updateCiclo(id, cicloToUpdate)
                    if (updated) {
                        val updatedCiclo = cicloService.getCicloById(id)
                        call.respond(HttpStatusCode.OK, updatedCiclo ?: "Ciclo updated but could not be retrieved.")
                    } else {
                        // This case might indicate an issue if the ID was valid but no rows were updated.
                        // Could be a concurrent modification or an issue with the update logic itself.
                        call.respond(HttpStatusCode.InternalServerError, "Ciclo with id $id found but update failed unexpectedly.")
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: IllegalArgumentException) { // From service (e.g. anio/numero conflict during update)
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: Exception) {
                    application.log.error("Failed to update ciclo $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update ciclo: ${e.localizedMessage}")
                }
            }

            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Ciclo ID format")
                    return@delete
                }
                
                if (cicloService.getCicloById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Ciclo with id $id not found for deletion.")
                    return@delete
                }

                val deleted = cicloService.deleteCiclo(id)
                if (deleted) {
                    call.respondText("Ciclo with id $id deleted successfully.", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Ciclo with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }
}

fun Application.configureCicloRouting(cicloService: CicloService) {
    routing {
        cicloRoutes(cicloService)
    }
}
