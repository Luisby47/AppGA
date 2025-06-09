package entities

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: Int? = null,
    val cedula: String,
    val clave: String, // Be mindful of storing plain text passwords; consider hashing in a real application.
    val rol: String
)