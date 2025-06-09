package entities

import kotlinx.serialization.Serializable

@Serializable
data class CarreraCurso(
    val codigoCarrera: String,
    val codigoCurso: String,
    val anio: Int,
    val ciclo: String
)
