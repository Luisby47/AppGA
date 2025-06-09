package config

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import repositories.DatabaseFactory
import services.*
import controllers.*


fun Application.module() {
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true 
        })
    }

    val alumnoService = AlumnoService()
    val carreraService = CarreraService()
    val cursoService = CursoService()
    val profesorService = ProfesorService()
    val usuarioService = UsuarioService()
    val cicloService = CicloService()
    val grupoService = GrupoService()
    val matriculaService = MatriculaService()
    val carreraCursoService = CarreraCursoService()

    configureAlumnoRouting(alumnoService)
    configureCarreraRouting(carreraService)
    configureCursoRouting(cursoService)
    configureProfesorRouting(profesorService)
    configureUsuarioRouting(usuarioService)
    configureCicloRouting(cicloService)
    configureGrupoRouting(grupoService)
    configureCarreraCursoRouting(carreraCursoService)
    configureMatriculaRoutes(matriculaService)


}

// fun main() { 
//     embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
//         module()
//     }.start(wait = true) 
// } 