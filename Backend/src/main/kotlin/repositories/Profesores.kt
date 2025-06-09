package repositories

import org.jetbrains.exposed.sql.Table

object Profesores : Table("profesores") {
    val id = integer("id").autoIncrement() // Primary key
    val cedula = varchar("cedula", 20)
    val nombre = varchar("nombre", 100).nullable()
    val telefono = varchar("telefono", 15).nullable()
    val email = varchar("email", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}
