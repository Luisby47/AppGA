package entities

import kotlinx.serialization.Serializable

@Serializable
data class Alumno(
    val id: Int?,
    val cedula: String,
    val nombre: String,
    val telefono: String?,
    val email: String,
    val fechaNacimiento: String, // Consider using LocalDate or a proper date type
    val codigoCarrera: String?
) {
    // toString() is auto-generated for data classes
    // Getters and setters are auto-generated for val/var properties
    // equals() and hashCode() are auto-generated
    // copy() method is auto-generated
}