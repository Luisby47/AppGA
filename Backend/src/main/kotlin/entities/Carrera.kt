package entities

import kotlinx.serialization.Serializable

@Serializable
data class Carrera(
    val id: Int? = null,
    val codigo: String,
    val nombre: String,
    val titulo: String
)
