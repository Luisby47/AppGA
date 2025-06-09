package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*     
import services.CarreraCursoService
import services.MatriculaService

// Main function to set up all CarreraCurso related routes
fun Application.configureCarreraCursoRouting(carreraCursoService: CarreraCursoService) {
    routing {
        carreraCursoRoutes(carreraCursoService)
    }
}

fun Route.carreraCursoRoutes(carreraCursoService: CarreraCursoService) {

    route("/carreras-cursos") { 
        // Get all associations (mainly for debugging or admin purposes)
        get {
            val associations = carreraCursoService.getAllAssociations()
            call.respond(associations)
        }
    }

    // Routes for operations related to a specific carrera's cursos
    route("/carreras/{carreraId}/cursos") {
        get {
            val carreraId = call.parameters["carreraId"]?.toIntOrNull()
            if (carreraId == null) {
                call.respond(HttpStatusCode.BadRequest, "Carrera ID must be an integer.")
                return@get
            }
            val cursos = carreraCursoService.getCursosByCarreraId(carreraId)
            // Ktor automatically handles empty list serialization to []
            call.respond(cursos)
        }

        // Associate a curso with a carrera
        post("/{cursoId}") {
            val carreraId = call.parameters["carreraId"]?.toIntOrNull()
            val cursoId = call.parameters["cursoId"]?.toIntOrNull()

            if (carreraId == null || cursoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Carrera ID and Curso ID must be integers.")
                return@post
            }

            val association = carreraCursoService.addCursoToCarrera(carreraId, cursoId)
            if (association != null) {
                call.respond(HttpStatusCode.Created, association)
            } else {
                // This could be due to non-existent carrera/curso or other DB issue (e.g., duplicate if not handled by service returning existing)
                call.respond(HttpStatusCode.Conflict, "Failed to associate curso with carrera. Check if both IDs exist or if association already exists.")
            }
        }

        // Disassociate a curso from a carrera
        delete("/{cursoId}") {
            val carreraId = call.parameters["carreraId"]?.toIntOrNull()
            val cursoId = call.parameters["cursoId"]?.toIntOrNull()

            if (carreraId == null || cursoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Carrera ID and Curso ID must be integers.")
                return@delete
            }

            val removed = carreraCursoService.removeCursoFromCarrera(carreraId, cursoId)
            if (removed) {
                call.respondText("Curso disassociated from carrera successfully.", status = HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound, "Failed to disassociate curso from carrera. Association might not exist.")
            }
        }
    }

    // Routes for operations related to a specific curso's carreras
    route("/cursos/{cursoId}/carreras") {
        get {
            val cursoId = call.parameters["cursoId"]?.toIntOrNull()
            if (cursoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Curso ID must be an integer.")
                return@get
            }
            val carreras = carreraCursoService.getCarrerasByCursoId(cursoId)
            // Ktor automatically handles empty list serialization to []
            call.respond(carreras)
        }
    }
}


