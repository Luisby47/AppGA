package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import entities.Grupo
import services.GrupoService

fun Route.grupoRoutes(grupoService: GrupoService) {

    route("/grupos") {
        get {
            val grupos = grupoService.getAllGrupos()
            call.respond(grupos) // Ktor handles empty list serialization
        }
        
        // Get grupos by curso ID
        get("/curso/{id}") {
            val cursoId = call.parameters["id"]?.toIntOrNull()
            if (cursoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Curso ID must be an integer")
                return@get
            }
            val grupos = grupoService.getGruposByCursoId(cursoId)
            call.respond(grupos)
        }
        
        // Get grupos by profesor cedula
        get("/profesor/{cedula}") {
            val cedulaProfesor = call.parameters["cedula"]
            if (cedulaProfesor.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Profesor cedula is required")
                return@get
            }
            val grupos = grupoService.getGruposByProfesorCedula(cedulaProfesor)
            call.respond(grupos)
        }

        post {
            try {
                val grupo = call.receive<Grupo>() // id in this object is ignored by service
                // Basic validation for required fields (non-nullable in entity)
                if (grupo.codigoCurso.isBlank()) { // Example, could add more for anio, numeroCiclo, numeroGrupo
                     call.respond(HttpStatusCode.BadRequest, "CodigoCurso, Anio, NumeroCiclo, and NumeroGrupo are required.")
                     return@post
                }

                val createdGrupo = grupoService.addGrupo(grupo)
                call.respond(HttpStatusCode.Created, createdGrupo)
            } catch (e: IllegalArgumentException) { // From service (e.g. composite key conflict)
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: Exception) {
                application.log.error("Failed to create grupo", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create grupo: ${e.localizedMessage}")
            }
        }

        // GET /grupos/byCompositeKey/{anio}/{numeroCiclo}/{codigoCurso}/{numeroGrupo}
        get("/byCompositeKey/{anio}/{numeroCiclo}/{codigoCurso}/{numeroGrupo}") {
            val anio = call.parameters["anio"]?.toIntOrNull()
            val numeroCiclo = call.parameters["numeroCiclo"]
            val codigoCurso = call.parameters["codigoCurso"]
            val numeroGrupo = call.parameters["numeroGrupo"]?.toIntOrNull()

            if (anio == null || numeroCiclo == null || codigoCurso == null || numeroGrupo == null) {
                call.respond(HttpStatusCode.BadRequest, "Parameters anio, numeroCiclo, codigoCurso, and numeroGrupo are required and must be valid.")
                return@get
            }

            val grupo = grupoService.getGrupoByCompositeKey(anio, numeroCiclo, codigoCurso, numeroGrupo)
            if (grupo != null) {
                call.respond(grupo)
            } else {
                call.respond(HttpStatusCode.NotFound, "Grupo with key ($anio, $numeroCiclo, $codigoCurso, $numeroGrupo) not found.")
            }
        }

        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID parameter must be an integer.")
                    return@get
                }
                val grupo = grupoService.getGrupoById(id)
                if (grupo != null) {
                    call.respond(grupo)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Grupo with id $id not found.")
                }
            }

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID parameter must be an integer for update.")
                    return@put
                }

                try {
                    val grupoToUpdate = call.receive<Grupo>()
                    // The 'id' in grupoToUpdate.id is ignored; 'id' from path is used.
                     if (grupoToUpdate.codigoCurso.isBlank()) { // Example validation
                        call.respond(HttpStatusCode.BadRequest, "CodigoCurso, Anio, NumeroCiclo, and NumeroGrupo are required for update.")
                        return@put
                    }

                    if (grupoService.getGrupoById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Grupo with id $id not found for update.")
                        return@put
                    }
                    
                    val updated = grupoService.updateGrupo(id, grupoToUpdate)
                    if (updated) {
                        val updatedGrupo = grupoService.getGrupoById(id)
                        call.respond(HttpStatusCode.OK, updatedGrupo ?: "Grupo updated but could not be retrieved.")
                    } else {
                        // This might happen if the data didn't actually change, or an unexpected issue.
                        // The service now throws IllegalArgumentException for conflicts, caught below.
                        call.respond(HttpStatusCode.NotModified, "Grupo with id $id was not modified or update failed due to other reasons.")
                    }
                } catch (e: IllegalArgumentException) { // From service (e.g. composite key conflict)
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: Exception) {
                    application.log.error("Failed to update grupo $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update grupo: ${e.localizedMessage}")
                }
            }

            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID parameter must be an integer for deletion.")
                    return@delete
                }
                 if (grupoService.getGrupoById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Grupo with id $id not found for deletion.")
                    return@delete
                }
                val deleted = grupoService.deleteGrupo(id)
                if (deleted) {
                    call.respondText("Grupo with id $id deleted successfully.", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Grupo with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }
}

fun Application.configureGrupoRouting(grupoService: GrupoService) {
    routing {
        grupoRoutes(grupoService)
    }
}
