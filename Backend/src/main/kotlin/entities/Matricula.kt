package entities

import kotlinx.serialization.Serializable

@Serializable
data class Matricula(
    val id: Int? = null, 
    val alumnoId: Int,    
    val grupoId: Int,         
    val nota: Int? = null     
)