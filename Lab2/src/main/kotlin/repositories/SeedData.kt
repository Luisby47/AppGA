package repositories

import services.*
import entities.*

suspend fun populateInitialData() {
    // Removed outer transaction block
    val carreraService = CarreraService()
    val cursoService = CursoService()
    val profesorService = ProfesorService()
    val cicloService = CicloService()
    val usuarioService = UsuarioService()
    val alumnoService = AlumnoService()
    val carreraCursoService = CarreraCursoService() // Ensure instantiated if used
    val grupoService = GrupoService()
    val matriculaService = MatriculaService() // Ensure instantiated if used

    // --- Seed Carreras ---
    val carreras = listOf(
        Carrera(
            id = null,
            codigo = "C001",
            nombre = "Ingeniería de Software",
            titulo = "Bachiller en Ingeniería de Software"
        ),
        Carrera(
            id = null,
            codigo = "C002",
            nombre = "Administración de Empresas",
            titulo = "Licenciado en Administración de Empresas"
        ),
        Carrera(id = null, codigo = "C003", nombre = "Diseño Gráfico", titulo = "Técnico en Diseño Gráfico")
    )
    // Store created carreras to get their IDs for associations
    val createdCarreras = mutableListOf<Carrera>()
    carreras.forEach { carrera ->
        val existingCarrera = carreraService.getCarreraByCodigo(carrera.codigo)
        if (existingCarrera == null) {
            val createdCarrera = carreraService.addCarrera(carrera)
            createdCarreras.add(createdCarrera)
        } else {
            createdCarreras.add(existingCarrera)
        }
    }

    // --- Seed Cursos ---
    val cursos = listOf(
        Curso(
            id = null,
            codigo = "CS101",
            nombre = "Programación Orientada a Objetos",
            creditos = 4,
            horasSemanales = 6
        ),
        Curso(id = null, codigo = "CS202", nombre = "Estructuras de Datos", creditos = 4, horasSemanales = 6),
        Curso(id = null, codigo = "BA101", nombre = "Contabilidad General", creditos = 3, horasSemanales = 5),
        Curso(id = null, codigo = "DG101", nombre = "Diseño Digital I", creditos = 3, horasSemanales = 4),
        Curso(id = null, codigo = "CS303", nombre = "Arquitectura de Software", creditos = 4, horasSemanales = 5)
    )
    // Store created cursos to get their IDs for associations
    val createdCursos = mutableListOf<Curso>()
    cursos.forEach { curso ->
        val existingCurso = cursoService.getCursoByCodigo(curso.codigo)
        if (existingCurso == null) {
            val createdCurso = cursoService.addCurso(curso)
            createdCursos.add(createdCurso)
        } else {
            createdCursos.add(existingCurso)
        }
    }

    // --- Seed Profesores ---
    val profesores = listOf(
        Profesor(
            id = null,
            cedula = "P001",
            nombre = "Dr. Alan Turing",
            email = "alan.turing@example.com",
            telefono = "11112222"
        ),
        Profesor(
            id = null,
            cedula = "P002",
            nombre = "Dra. Ada Lovelace",
            email = "ada.lovelace@example.com",
            telefono = "33334444"
        )
    )
    profesores.forEach { profesor ->
        if (profesorService.getProfesorByCedula(profesor.cedula) == null) {
            profesorService.addProfesor(profesor)
        }
    }

    // --- Seed Ciclos ---
    val ciclos = listOf(
        Ciclo(id = null, anio = 2024, numero = "1", fechaInicio = "2024-03-01", fechaFin = "2024-07-15", activo = true),
        Ciclo(id = null, anio = 2024, numero = "2", fechaInicio = "2024-08-01", fechaFin = "2024-12-15", activo = true)
    )
    ciclos.forEach { ciclo ->
        val existingCiclo = cicloService.getCicloByAnioAndNumero(ciclo.anio, ciclo.numero)
        if (existingCiclo == null) {
            cicloService.addCiclo(ciclo)
        }
    }

    // --- Seed Usuarios (some will be Alumnos, some Profesores) ---
    val usuarios = listOf(
        Usuario(id = null, cedula = "user001", clave = "password123", rol = "alumno"),
        Usuario(id = null, cedula = "user002", clave = "password456", rol = "alumno"),
        Usuario(id = null, cedula = "user003", clave = "profpass", rol = "profesor"),
        Usuario(id = null, cedula = "admin01", clave = "adminpass", rol = "administrador")
    )
    usuarios.forEach { usuario ->
        if (usuarioService.getUsuarioByCedula(usuario.cedula) == null) {
            usuarioService.addUsuario(usuario)
        }
    }

    // --- Seed Alumnos ---
    val alumnos = listOf(
        Alumno(
            id = null,
            cedula = "A001",
            nombre = "Alice Wonderland",
            telefono = "88887777",
            email = "alice@example.com",
            fechaNacimiento = "2002-05-10",
            codigoCarrera = "C001"
        ),
        Alumno(
            id = null,
            cedula = "A002",
            nombre = "Bob The Builder",
            telefono = "66665555",
            email = "bob@example.com",
            fechaNacimiento = "2001-09-20",
            codigoCarrera = "C002"
        )
    )
    // Store created alumnos to get their IDs for matriculas
    val createdAlumnos = mutableListOf<Alumno>()
    alumnos.forEach { alumno ->
        val existingAlumno = alumnoService.getAlumnoByCedula(alumno.cedula)
        if (existingAlumno == null) {
            val createdAlumno = alumnoService.addAlumno(alumno)
            createdAlumnos.add(createdAlumno)
        } else {
            createdAlumnos.add(existingAlumno)
        }
    }

    // --- Seed CarreraCursos (Associations) ---
    // Map codigo to id for both carreras and cursos
    val carreraCodigoToId = createdCarreras.associate { it.codigo to it.id!! }
    val cursoCodigoToId = createdCursos.associate { it.codigo to it.id!! }

    val carreraCursosAssociations = listOf(
        Pair("C001", "CS101"),
        Pair("C001", "CS202"),
        Pair("C001", "CS303"),
        Pair("C002", "BA101"),
        Pair("C002", "CS101"),
        Pair("C003", "DG101")
    )

    carreraCursosAssociations.forEach { (carreraCodigo, cursoCodigo) ->
        val carreraId = carreraCodigoToId[carreraCodigo]
        val cursoId = cursoCodigoToId[cursoCodigo]

        if (carreraId != null && cursoId != null) {
            carreraCursoService.addCursoToCarrera(carreraId, cursoId)
        } else {
            println("Warning: Could not associate carrera $carreraCodigo with curso $cursoCodigo - IDs not found")
        }
    }

    // --- Seed Grupos ---
    val gruposToSeed = listOf(
        Grupo(
            id = null,
            anio = 2024,
            numeroCiclo = "1",
            codigoCurso = "CS101",
            numeroGrupo = 1,
            horario = "L/W 08:00-10:00",
            cedulaProfesor = "P001"
        ),
        Grupo(
            id = null,
            anio = 2024,
            numeroCiclo = "1",
            codigoCurso = "BA101",
            numeroGrupo = 2,
            horario = "M/J 10:00-12:00",
            cedulaProfesor = "P002"
        )
    )
    val createdGrupos = mutableListOf<Grupo>()
    gruposToSeed.forEach { grupoData ->
        val addedGrupo = grupoService.addGrupo(grupoData)
        createdGrupos.add(addedGrupo)
    }

    // --- Seed Matriculas ---
    // Map cedula to id for alumnos
    val alumnosCedulaToId = createdAlumnos.associate { it.cedula to it.id!! }

    val matriculasData = listOf(
        Triple("A001", "CS101", 95), // alumno cedula, curso codigo, nota
        Triple("A002", "BA101", 88)  // alumno cedula, curso codigo, nota
    )

    matriculasData.forEach { (alumnoCedula, cursoCodigo, nota) ->
        val alumnoId = alumnosCedulaToId[alumnoCedula]
        val grupo = createdGrupos.find { it.codigoCurso == cursoCodigo && it.anio == 2024 && it.numeroCiclo == "1" }

        if (alumnoId != null && grupo != null) {
            val matricula = Matricula(
                id = null,
                alumnoId = alumnoId,
                grupoId = grupo.id!!,
                nota = nota
            )
            try {
                matriculaService.createMatricula(matricula)
            } catch (e: Exception) {
                println("Warning: Could not create matricula for alumno $alumnoCedula in curso $cursoCodigo: ${e.message}")
            }
        } else {
            println("Warning: Could not create matricula - alumno ID or grupo not found for $alumnoCedula/$cursoCodigo")
        }
    }

    println("Initial data populated if tables were empty.")
}