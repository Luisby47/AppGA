package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import entities.Curso
import services.CursoService

fun Route.cursoRoutes(cursoService: CursoService) {

    route("/cursos") {
        get {
            val cursos = cursoService.getAllCursos()
            call.respond(cursos) // Ktor handles empty list serialization
        }

        post {
            try {
                val curso = call.receive<Curso>() // id in this object is ignored
                if (curso.codigo.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Codigo cannot be empty")
                    return@post
                }
                // Check if curso with this codigo already exists
                if (cursoService.getCursoByCodigo(curso.codigo) != null) {
                    call.respond(HttpStatusCode.Conflict, "Curso with codigo ${curso.codigo} already exists.")
                    return@post
                }
                val createdCurso = cursoService.addCurso(curso)
                call.respond(HttpStatusCode.Created, createdCurso)
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: IllegalArgumentException) { // From service if codigo conflict
                call.respond(HttpStatusCode.Conflict, e.localizedMessage)
            } catch (e: Exception) {
                application.log.error("Failed to create curso", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create curso: ${e.localizedMessage}")
            }
        }

        // GET /cursos/codigo/{codigo} - Get a curso by codigo
        get("/codigo/{codigo}") {
            val codigo = call.parameters["codigo"]
            if (codigo == null) {
                call.respond(HttpStatusCode.BadRequest, "Codigo parameter is missing")
                return@get
            }
            val curso = cursoService.getCursoByCodigo(codigo)
            if (curso != null) {
                call.respond(curso)
            } else {
                call.respond(HttpStatusCode.NotFound, "Curso with codigo $codigo not found")
            }
        }

        // Route for operations on a specific curso by ID
        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Curso ID format")
                    return@get
                }
                val curso = cursoService.getCursoById(id)
                if (curso != null) {
                    call.respond(curso)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Curso with id $id not found")
                }
            }

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Curso ID format")
                    return@put
                }
                try {
                    val cursoToUpdate = call.receive<Curso>()
                    
                    if (cursoService.getCursoById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Curso with id $id not found for update.")
                        return@put
                    }

                    // Optional: If codigo can be updated, check for conflicts
                    // val currentCurso = cursoService.getCursoById(id)!!
                    // if (cursoToUpdate.codigo != currentCurso.codigo) {
                    //     if (cursoService.getCursoByCodigo(cursoToUpdate.codigo) != null) {
                    //         call.respond(HttpStatusCode.Conflict, "Another curso with codigo ${cursoToUpdate.codigo} already exists.")
                    //         return@put
                    //     }
                    // }

                    val updated = cursoService.updateCurso(id, cursoToUpdate)
                    if (updated) {
                        val updatedCurso = cursoService.getCursoById(id)
                        call.respond(HttpStatusCode.OK, updatedCurso ?: "Curso updated but could not be retrieved.")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Curso with id $id found but update failed unexpectedly.")
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: IllegalArgumentException) { // From service if codigo conflict
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: Exception) {
                    application.log.error("Failed to update curso $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update curso: ${e.localizedMessage}")
                }
            }

            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Curso ID format")
                    return@delete
                }
                if (cursoService.getCursoById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Curso with id $id not found for deletion.")
                    return@delete
                }
                val deleted = cursoService.deleteCurso(id)
                if (deleted) {
                    call.respondText("Curso with id $id deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Curso with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }
}

fun Application.configureCursoRouting(cursoService: CursoService) {
    routing {
        cursoRoutes(cursoService)


    }
}
