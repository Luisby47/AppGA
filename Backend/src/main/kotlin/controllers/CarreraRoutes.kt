package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import entities.Carrera
import services.CarreraService

fun Route.carreraRoutes(carreraService: CarreraService) {

    route("/carreras") {
        // GET /carreras - Get all carreras
        get {
            val carreras = carreraService.getAllCarreras()
            call.respond(carreras) // Ktor handles empty list serialization
        }

        // POST /carreras - Create a new carrera
        post {
            try {
                val carrera = call.receive<Carrera>() // id in this object is ignored
                if (carrera.codigo.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Codigo cannot be empty")
                    return@post
                }
                // Check if carrera with this codigo already exists
                if (carreraService.getCarreraByCodigo(carrera.codigo) != null) {
                    call.respond(HttpStatusCode.Conflict, "Carrera with codigo ${carrera.codigo} already exists.")
                    return@post
                }
                val createdCarrera = carreraService.addCarrera(carrera)
                call.respond(HttpStatusCode.Created, createdCarrera)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: IllegalArgumentException) { // From service if codigo conflict during add (if service checks)
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } catch (e: Exception) {
                application.log.error("Failed to create carrera", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create carrera: ${e.localizedMessage}")
            }
        }

        // GET /carreras/codigo/{codigo} - Get a carrera by codigo
        get("/codigo/{codigo}") {
            val codigo = call.parameters["codigo"]
            if (codigo == null) {
                call.respond(HttpStatusCode.BadRequest, "Codigo parameter is missing")
                return@get
            }
            val carrera = carreraService.getCarreraByCodigo(codigo)
            if (carrera != null) {
                call.respond(carrera)
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrera with codigo $codigo not found")
            }
        }

        // Route for operations on a specific carrera by ID
        route("/{id}") {
            // GET /carreras/{id} - Get a carrera by ID
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Carrera ID format")
                    return@get
                }
                val carrera = carreraService.getCarreraById(id)
                if (carrera != null) {
                    call.respond(carrera)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Carrera with id $id not found")
                }
            }

            // PUT /carreras/{id} - Update a carrera by ID
            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Carrera ID format")
                    return@put
                }
                try {
                    val carreraToUpdate = call.receive<Carrera>()
                    
                    if (carreraService.getCarreraById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Carrera with id $id not found for update.")
                        return@put
                    }

                    // Optional: If codigo can be updated, check for conflicts
                    // val currentCarrera = carreraService.getCarreraById(id)!!
                    // if (carreraToUpdate.codigo != currentCarrera.codigo) {
                    //     if (carreraService.getCarreraByCodigo(carreraToUpdate.codigo) != null) {
                    //         call.respond(HttpStatusCode.Conflict, "Another carrera with codigo ${carreraToUpdate.codigo} already exists.")
                    //         return@put
                    //     }
                    // }

                    val updated = carreraService.updateCarrera(id, carreraToUpdate)
                    if (updated) {
                        val updatedCarrera = carreraService.getCarreraById(id)
                        call.respond(HttpStatusCode.OK, updatedCarrera ?: "Carrera updated but could not be retrieved.")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Carrera with id $id found but update failed unexpectedly.")
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: IllegalArgumentException) { // From service if codigo conflict during update
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: Exception) {
                    application.log.error("Failed to update carrera $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update carrera: ${e.localizedMessage}")
                }
            }

            // DELETE /carreras/{id} - Delete a carrera by ID
            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Carrera ID format")
                    return@delete
                }
                if (carreraService.getCarreraById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Carrera with id $id not found for deletion.")
                    return@delete
                }
                val deleted = carreraService.deleteCarrera(id)
                if (deleted) {
                    call.respondText("Carrera with id $id deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Carrera with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }
}

fun Application.configureCarreraRouting(carreraService: CarreraService) {
    routing {
        carreraRoutes(carreraService)
    }
}
