package controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import entities.Usuario
import services.UsuarioService

@Serializable
data class LoginRequest(
    val cedula: String,
    val clave: String
)

@Serializable
data class LoginResponse(
    val user: Usuario,
    val token: String? = null
)

fun Route.usuarioRoutes(usuarioService: UsuarioService) {
    
    // Login route
    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            
            if (loginRequest.cedula.isBlank() || loginRequest.clave.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Cedula and clave are required")
                return@post
            }
            
            val authenticatedUser = usuarioService.authenticate(loginRequest.cedula, loginRequest.clave)
            
            if (authenticatedUser != null) {
                call.respond(
                    HttpStatusCode.OK,
                    LoginResponse(
                        user = authenticatedUser,
                        token = "jwt-token-placeholder"
                    )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        } catch (e: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest, "Invalid request format: ${e.localizedMessage}")
        } catch (e: Exception) {
            application.log.error("Login error", e)
            call.respond(HttpStatusCode.InternalServerError, "Login failed: ${e.localizedMessage}")
        }
    }



    route("/usuarios") {
        get {
            val usuarios = usuarioService.getAllUsuarios()
            call.respond(usuarios) // Ktor handles empty list serialization
        }



        post {
            try {
                val usuario = call.receive<Usuario>() // id in this object is ignored by service
                if (usuario.cedula.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Cedula cannot be empty for a new usuario")
                    return@post
                }
                // Check if usuario with this cedula already exists
                if (usuarioService.getUsuarioByCedula(usuario.cedula) != null) {
                    call.respond(HttpStatusCode.Conflict, "Usuario with cedula ${usuario.cedula} already exists.")
                    return@post
                }
                // Reminder: Password (clave) is stored as plain text by the service.
                val createdUsuario = usuarioService.addUsuario(usuario)
                call.respond(HttpStatusCode.Created, createdUsuario)
            } catch (e: IllegalArgumentException) { // From service (e.g. cedula constraint if service throws)
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid input for usuario")
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
            } catch (e: Exception) {
                application.log.error("Failed to create usuario", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to create usuario: ${e.localizedMessage}")
            }
        }

        // GET /usuarios/cedula/{cedula} - Get a usuario by cedula
        get("/cedula/{cedula}") {
            val cedula = call.parameters["cedula"]
            if (cedula == null) {
                call.respond(HttpStatusCode.BadRequest, "Cedula parameter is missing")
                return@get
            }
            val usuario = usuarioService.getUsuarioByCedula(cedula)
            if (usuario != null) {
                call.respond(usuario)
            } else {
                call.respond(HttpStatusCode.NotFound, "Usuario with cedula $cedula not found")
            }
        }

        // Route for operations on a specific usuario by ID
        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Usuario ID format")
                    return@get
                }
                val usuario = usuarioService.getUsuarioById(id)
                if (usuario != null) {
                    call.respond(usuario)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Usuario with id $id not found")
                }
            }

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Usuario ID format")
                    return@put
                }
                try {
                    val usuarioToUpdate = call.receive<Usuario>()
                    
                    if (usuarioService.getUsuarioById(id) == null) {
                        call.respond(HttpStatusCode.NotFound, "Usuario with id $id not found for update.")
                        return@put
                    }

                    // The service's updateUsuario allows updating cedula, clave, and rol.
                    // If service throws IllegalArgumentException (e.g. for cedula conflict), it's caught below.
                    val updated = usuarioService.updateUsuario(id, usuarioToUpdate)
                    if (updated) {
                        val updatedUsuario = usuarioService.getUsuarioById(id)
                        call.respond(HttpStatusCode.OK, updatedUsuario ?: "Usuario updated but could not be retrieved.")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Usuario with id $id found but update failed unexpectedly.")
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.localizedMessage}")
                } catch (e: IllegalArgumentException) { // From service (e.g. cedula conflict during update)
                    call.respond(HttpStatusCode.Conflict, e.localizedMessage)
                } catch (e: Exception) {
                    application.log.error("Failed to update usuario $id", e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update usuario: ${e.localizedMessage}")
                }
            }

            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid Usuario ID format")
                    return@delete
                }
                if (usuarioService.getUsuarioById(id) == null) {
                    call.respond(HttpStatusCode.NotFound, "Usuario with id $id not found for deletion.")
                    return@delete
                }
                val deleted = usuarioService.deleteUsuario(id)
                if (deleted) {
                    call.respondText("Usuario with id $id deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Usuario with id $id found but delete failed unexpectedly.")
                }
            }
        }
    }


}

fun Application.configureUsuarioRouting(usuarioService: UsuarioService) {
    routing {
        usuarioRoutes(usuarioService)
    }
}
