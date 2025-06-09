package repositories

import org.jetbrains.exposed.sql.Table

object Usuarios : Table("usuarios") {
    val id = integer("id").autoIncrement() // Primary key
    val cedula = varchar("cedula", 20) // Primary key
    val clave = varchar("clave", 255) // Consider length for hashed passwords if implemented
    val rol = varchar("rol", 50)

    override val primaryKey = PrimaryKey(id)
}
