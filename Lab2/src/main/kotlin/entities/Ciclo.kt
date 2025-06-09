package entities

import kotlinx.serialization.Serializable

@Serializable
data class Ciclo (
    val id: Int? = null,
    val anio: Int, 
    val numero: String,     
    val fechaInicio: String?, 
    val fechaFin: String?, 
    val activo: Boolean 
)
