package com.example.appga.network

import com.example.appga.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    // For emulator: 10.0.2.2 is a special alias to the host machine
    // For physical device: Use your computer's actual IP address on the network
    private val baseUrl = "https://gabackend-1kmz.onrender.com" // Use this for emulator

    // Authentication
    suspend fun login(cedula: String, clave: String): Result<Usuario> {
        return try {
            println("DEBUG: Attempting login with cedula: $cedula")
            val response = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(cedula, clave))
            }
            println("DEBUG: Login API response status: ${response.status}")
            val loginResponse = response.body<LoginResponse>()
            println("DEBUG: Raw LoginResponse: $loginResponse")
            println("DEBUG: User from response - ID: ${loginResponse.user.id}, Cedula: ${loginResponse.user.cedula}, Rol: ${loginResponse.user.rol}")
            Result.success(loginResponse.user)
        } catch (e: Exception) {
            println("DEBUG: Login API error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Alumno endpoints
    suspend fun getAllAlumnos(): Result<List<Alumno>> {
        return try {
            val response = client.get("$baseUrl/alumnos")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlumnoById(id: Int): Result<Alumno> {
        return try {
            val response = client.get("$baseUrl/alumnos/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlumnoByCedula(cedula: String): Result<Alumno> {
        return try {
            val response = client.get("$baseUrl/alumnos/cedula/$cedula")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAlumno(alumno: Alumno): Result<Alumno> {
        return try {
            val response = client.post("$baseUrl/alumnos") {
                contentType(ContentType.Application.Json)
                setBody(alumno)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAlumno(id: Int, alumno: Alumno): Result<Alumno> {
        return try {
            val response = client.put("$baseUrl/alumnos/$id") {
                contentType(ContentType.Application.Json)
                setBody(alumno)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAlumno(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/alumnos/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Carrera endpoints
    suspend fun getAllCarreras(): Result<List<Carrera>> {
        return try {
            val response = client.get("$baseUrl/carreras")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCarreraById(id: Int): Result<Carrera> {
        return try {
            val response = client.get("$baseUrl/carreras/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCarreraByCodigo(codigo: String): Result<Carrera> {
        return try {
            val response = client.get("$baseUrl/carreras/codigo/$codigo")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCarrera(carrera: Carrera): Result<Carrera> {
        return try {
            val response = client.post("$baseUrl/carreras") {
                contentType(ContentType.Application.Json)
                setBody(carrera)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCarrera(id: Int, carrera: Carrera): Result<Carrera> {
        return try {
            val response = client.put("$baseUrl/carreras/$id") {
                contentType(ContentType.Application.Json)
                setBody(carrera)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCarrera(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/carreras/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Curso endpoints
    suspend fun getAllCursos(): Result<List<Curso>> {
        return try {
            val response = client.get("$baseUrl/cursos")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCursoById(id: Int): Result<Curso> {
        return try {
            val response = client.get("$baseUrl/cursos/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCursoByCodigo(codigo: String): Result<Curso> {
        return try {
            val response = client.get("$baseUrl/cursos/codigo/$codigo")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCurso(curso: Curso): Result<Curso> {
        return try {
            val response = client.post("$baseUrl/cursos") {
                contentType(ContentType.Application.Json)
                setBody(curso)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCurso(id: Int, curso: Curso): Result<Curso> {
        return try {
            val response = client.put("$baseUrl/cursos/$id") {
                contentType(ContentType.Application.Json)
                setBody(curso)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCurso(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/cursos/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CarreraCurso endpoints
    suspend fun getAllCarreraCursos(): Result<List<CarreraCurso>> {
        return try {
            val response = client.get("$baseUrl/carrera-cursos")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCarreraCursosByCarrera(codigoCarrera: String): Result<List<CarreraCurso>> {
        return try {
            val response = client.get("$baseUrl/carrera-cursos/carrera/$codigoCarrera")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCursoToCarrera(carreraCurso: CarreraCurso): Result<CarreraCurso> {
        return try {
            val response = client.post("$baseUrl/carrera-cursos") {
                contentType(ContentType.Application.Json)
                setBody(carreraCurso)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCarreraCurso(id: Int, carreraCurso: CarreraCurso): Result<CarreraCurso> {
        return try {
            val response = client.put("$baseUrl/carrera-cursos/$id") {
                contentType(ContentType.Application.Json)
                setBody(carreraCurso)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCursoFromCarrera(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/carrera-cursos/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Profesor endpoints
    suspend fun getAllProfesores(): Result<List<Profesor>> {
        return try {
            val response = client.get("$baseUrl/profesores")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfesorById(id: Int): Result<Profesor> {
        return try {
            val response = client.get("$baseUrl/profesores/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfesorByCedula(cedula: String): Result<Profesor> {
        return try {
            val response = client.get("$baseUrl/profesores/cedula/$cedula")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProfesor(profesor: Profesor): Result<Profesor> {
        return try {
            val response = client.post("$baseUrl/profesores") {
                contentType(ContentType.Application.Json)
                setBody(profesor)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfesor(id: Int, profesor: Profesor): Result<Profesor> {
        return try {
            val response = client.put("$baseUrl/profesores/$id") {
                contentType(ContentType.Application.Json)
                setBody(profesor)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfesor(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/profesores/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ciclo endpoints
    suspend fun getAllCiclos(): Result<List<Ciclo>> {
        return try {
            val response = client.get("$baseUrl/ciclos")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCicloById(id: Int): Result<Ciclo> {
        return try {
            val response = client.get("$baseUrl/ciclos/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCiclosByAnio(anio: Int): Result<List<Ciclo>> {
        return try {
            val response = client.get("$baseUrl/ciclos/anio/$anio")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveCiclo(): Result<Ciclo> {
        return try {
            val response = client.get("$baseUrl/ciclos/activo")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCiclo(ciclo: Ciclo): Result<Ciclo> {
        return try {
            val response = client.post("$baseUrl/ciclos") {
                contentType(ContentType.Application.Json)
                setBody(ciclo)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCiclo(id: Int, ciclo: Ciclo): Result<Ciclo> {
        return try {
            val response = client.put("$baseUrl/ciclos/$id") {
                contentType(ContentType.Application.Json)
                setBody(ciclo)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setActiveCiclo(id: Int): Result<Ciclo> {
        return try {
            val response = client.put("$baseUrl/ciclos/$id/activo") {
                contentType(ContentType.Application.Json)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCiclo(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/ciclos/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Grupo endpoints
    suspend fun getAllGrupos(): Result<List<Grupo>> {
        return try {
            val response = client.get("$baseUrl/grupos")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGrupoById(id: Int): Result<Grupo> {
        return try {
            val response = client.get("$baseUrl/grupos/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGruposByProfesor(cedulaProfesor: String): Result<List<Grupo>> {
        return try {
            val response = client.get("$baseUrl/grupos/profesor/$cedulaProfesor")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGruposByCurso(codigoCurso: String): Result<List<Grupo>> {
        return try {
            val response = client.get("$baseUrl/grupos/curso/$codigoCurso")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGruposByCiclo(anio: Int, numeroCiclo: String): Result<List<Grupo>> {
        return try {
            val response = client.get("$baseUrl/grupos/ciclo/$anio/$numeroCiclo")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGrupo(grupo: Grupo): Result<Grupo> {
        return try {
            val response = client.post("$baseUrl/grupos") {
                contentType(ContentType.Application.Json)
                setBody(grupo)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGrupo(id: Int, grupo: Grupo): Result<Grupo> {
        return try {
            val response = client.put("$baseUrl/grupos/$id") {
                contentType(ContentType.Application.Json)
                setBody(grupo)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGrupo(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/grupos/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Matricula endpoints
    suspend fun getAllMatriculas(): Result<List<Matricula>> {
        return try {
            val response = client.get("$baseUrl/matriculas")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatriculaById(id: Int): Result<Matricula> {
        return try {
            val response = client.get("$baseUrl/matriculas/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatriculasByAlumno(alumnoId: Int): Result<List<Matricula>> {
        return try {
            val response = client.get("$baseUrl/matriculas/alumno/$alumnoId")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatriculasByGrupo(grupoId: Int): Result<List<Matricula>> {
        return try {
            val response = client.get("$baseUrl/matriculas/grupo/$grupoId")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMatricula(matricula: Matricula): Result<Matricula> {
        return try {
            val response = client.post("$baseUrl/matriculas") {
                contentType(ContentType.Application.Json)
                setBody(matricula)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMatricula(id: Int, matricula: Matricula): Result<Matricula> {
        return try {
            val response = client.put("$baseUrl/matriculas/$id") {
                contentType(ContentType.Application.Json)
                setBody(matricula)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMatricula(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/matriculas/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Usuario endpoints
    suspend fun getAllUsuarios(): Result<List<Usuario>> {
        return try {
            val response = client.get("$baseUrl/usuarios")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsuarioById(id: Int): Result<Usuario> {
        return try {
            val response = client.get("$baseUrl/usuarios/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsuarioByCedula(cedula: String): Result<Usuario> {
        return try {
            val response = client.get("$baseUrl/usuarios/cedula/$cedula")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val response = client.post("$baseUrl/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(usuario)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsuario(id: Int, usuario: Usuario): Result<Usuario> {
        return try {
            val response = client.put("$baseUrl/usuarios/$id") {
                contentType(ContentType.Application.Json)
                setBody(usuario)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUsuario(id: Int): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/usuarios/$id")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
