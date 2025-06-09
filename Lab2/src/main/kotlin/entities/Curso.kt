package entities

import kotlinx.serialization.Serializable

@Serializable
data class Curso(
    val id: Int? = null,
    val codigo: String,
    val nombre: String,
    val creditos: Int,
    val horasSemanales: Int
)