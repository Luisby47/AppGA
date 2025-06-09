package entities

import kotlinx.serialization.Serializable

@Serializable
data class Grupo(
    val id: Int? = null, // Made id nullable
    val anio: Int,
    val numeroCiclo: String,
    val codigoCurso: String,
    val numeroGrupo: Int,
    val horario: String?,
    val cedulaProfesor: String?
)