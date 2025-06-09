package com.example.appga.model

import kotlinx.serialization.Serializable

@Serializable
data class Alumno(
    val id: Int? = null,
    val cedula: String = "",
    val nombre: String = "",
    val telefono: String? = null,
    val email: String = "",
    val fechaNacimiento: String = "", 
    val codigoCarrera: String? = null
)

@Serializable
data class Carrera(
    val id: Int? = null,
    val codigo: String = "",
    val nombre: String = "",
    val titulo: String = ""
)

@Serializable
data class CarreraCurso(
    val id: Int? = null,
    val codigoCarrera: String = "",
    val codigoCurso: String = "",
    val orden: Int = 0
)

@Serializable
data class Ciclo(
    val id: Int? = null,
    val anio: Int = 0, 
    val numero: String = "",     
    val fechaInicio: String? = null, 
    val fechaFin: String? = null, 
    val activo: Boolean = false
)

@Serializable
data class Curso(
    val id: Int? = null,
    val codigo: String = "",
    val nombre: String = "",
    val creditos: Int = 0,
    val horasSemanales: Int = 0
)

@Serializable
data class Grupo(
    val id: Int? = null,
    val anio: Int = 0,
    val numeroCiclo: String = "",
    val codigoCurso: String = "",
    val numeroGrupo: Int = 0,
    val horario: String? = null,
    val cedulaProfesor: String? = null
)

@Serializable
data class Matricula(
    val id: Int? = null, 
    val alumnoId: Int = 0,    
    val grupoId: Int = 0,         
    val nota: Int? = null     
)

@Serializable
data class Profesor(
    val id: Int? = null,
    val cedula: String = "",
    val nombre: String = "",
    val telefono: String? = null,
    val email: String = ""
)

@Serializable
data class Usuario(
    val id: Int? = null,
    val cedula: String = "",
    val clave: String = "",
    val rol: String = ""
)

// UI helper models

// Model for showing a curso with its name and other attributes in the UI
data class CursoWithDetails(
    val curso: Curso,
    val carreraNombre: String? = null
)

// Model for showing a student with their enrollment details
data class AlumnoWithEnrollment(
    val alumno: Alumno,
    val carreraNombre: String? = null,
    val matriculas: List<MatriculaWithDetails> = emptyList()
)

// Model for showing matricula with course and group details
data class MatriculaWithDetails(
    val matricula: Matricula,
    val curso: Curso? = null,
    val grupo: Grupo? = null,
    val ciclo: Ciclo? = null
)

// Model for showing grupo with course and instructor details
data class GrupoWithDetails(
    val grupo: Grupo,
    val curso: Curso? = null,
    val profesor: Profesor? = null,
    val matriculas: List<MatriculaWithStudent> = emptyList()
)

// Model for showing matricula with student details
data class MatriculaWithStudent(
    val matricula: Matricula,
    val alumno: Alumno? = null
)

// Model for showing carrera with its courses
data class CarreraWithCursos(
    val carrera: Carrera,
    val cursos: List<CursoWithOrder> = emptyList()
)

// Model for showing a curso with its order in a carrera
data class CursoWithOrder(
    val curso: Curso,
    val orden: Int
)

// Login request model
@Serializable
data class LoginRequest(
    val cedula: String,
    val clave: String
)

// Login response model
@Serializable
data class LoginResponse(
    val user: Usuario,
    val token: String? = null
)

// Resource state for handling UI states
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
