package entities

import kotlinx.serialization.Serializable

@Serializable
data class Profesor (
    val id : Int?,
    val cedula: String,
    val nombre: String?,
    val telefono: String?,
    val email: String?
)