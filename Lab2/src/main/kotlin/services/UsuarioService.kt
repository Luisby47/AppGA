package services

import entities.Usuario
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import repositories.DatabaseFactory.dbQuery
import repositories.Usuarios


class UsuarioService {

    // IMPORTANT: In a real application, passwords (clave) should be securely hashed before storing.
    // This service currently stores them as plain text as per the Usuario entity definition.

    suspend fun getAllUsuarios(): List<Usuario> = dbQuery {
        Usuarios.selectAll().map { toUsuario(it) }
    }

    // Keep getUsuarioByCedula for uniqueness checks or specific lookups
    suspend fun getUsuarioByCedula(cedula: String): Usuario? = dbQuery {
        Usuarios.select { Usuarios.cedula eq cedula }
            .mapNotNull { toUsuario(it) }
            .singleOrNull()
    }

    // New method to get Usuario by primary key 'id'
    suspend fun getUsuarioById(id: Int): Usuario? = dbQuery {
        Usuarios.select { Usuarios.id eq id }
            .mapNotNull { toUsuario(it) }
            .singleOrNull()
    }

    suspend fun addUsuario(usuario: Usuario): Usuario {
        // Optional: Check if cedula already exists before insert
        // val existingUsuario = getUsuarioByCedula(usuario.cedula)
        // if (existingUsuario != null) {
        //     throw IllegalArgumentException("Usuario with cedula ${usuario.cedula} already exists.")
        // }
        return dbQuery {
            val newId = Usuarios.insert {
                it[cedula] = usuario.cedula
                it[clave] = usuario.clave // Store plain text password
                it[rol] = usuario.rol
            } get Usuarios.id // Get the auto-generated ID

            // Fetch the newly created usuario by its ID
            Usuarios.select { Usuarios.id eq newId }
                .mapNotNull { toUsuario(it) }
                .singleOrNull() ?: throw IllegalStateException("Failed to retrieve usuario after insert with id $newId")
        }
    }

    suspend fun updateUsuario(id: Int, usuarioUpdate: Usuario): Boolean {
        // Optional: If cedula is being updated, ensure the new cedula isn't already taken by another usuario.
        // val currentUsuario = getUsuarioById(id)
        // if (currentUsuario != null && usuarioUpdate.cedula != currentUsuario.cedula) {
        //     val existingUsuarioWithNewCedula = getUsuarioByCedula(usuarioUpdate.cedula)
        //     if (existingUsuarioWithNewCedula != null && existingUsuarioWithNewCedula.id != id) {
        //         throw IllegalArgumentException("Another usuario with cedula ${usuarioUpdate.cedula} already exists.")
        //     }
        // }
        return dbQuery {
            Usuarios.update({ Usuarios.id eq id }) {
                // Allow updating cedula, clave, and rol.
                // In a real app, if cedula is updated, ensure uniqueness.
                // If clave is updated, it should be re-hashed.
                it[Usuarios.cedula] = usuarioUpdate.cedula
                it[Usuarios.clave] = usuarioUpdate.clave
                it[Usuarios.rol] = usuarioUpdate.rol
            } > 0
        }
    }

    suspend fun deleteUsuario(id: Int): Boolean {
        return dbQuery {
            Usuarios.deleteWhere { Usuarios.id eq id } > 0
        }
    }

    private fun toUsuario(row: ResultRow): Usuario =
        Usuario(
            id = row[Usuarios.id],
            cedula = row[Usuarios.cedula],
            clave = row[Usuarios.clave],
            rol = row[Usuarios.rol]
        )
    
    // Method for authentication
    suspend fun authenticate(cedula: String, providedClave: String): Usuario? {
        val usuario = getUsuarioByCedula(cedula)
        // In a real app, compare hashed providedClave with stored hashed clave
        return if (usuario != null && usuario.clave == providedClave) {
            usuario
        } else {
            null
        }
    }
}
