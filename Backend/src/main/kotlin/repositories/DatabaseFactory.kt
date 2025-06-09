package repositories

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    // Define all your table objects here so they can be created
    private val tables = arrayOf(
        Alumnos, 
        Carreras, 
        Cursos, 
        Profesores, 
        Usuarios, 
        Ciclos, 
        Grupos, 
        CarreraCursos, 
        Matriculas // Add Matriculas table here
        // Add other table objects here as we create them
    )

    fun init() {
        val dbFile = File("lab2.db") // Creates the DB file in the project root (or current working directory)
        // Ensure the parent directory exists if you choose a path like "data/lab2.db"
        // dbFile.parentFile?.mkdirs()
        val db = Database.connect("jdbc:sqlite:${dbFile.canonicalPath}", "org.sqlite.JDBC")
        // For SQLite, it's often recommended to set journal_mode to WAL and synchronous to NORMAL for better performance and concurrency.
        // This can be done via PRAGMA statements after connection, or connection string parameters if supported by the driver version.
        // Example: "jdbc:sqlite:${dbFile.canonicalPath}?journal_mode=WAL&synchronous=NORMAL"
        // However, for simplicity, we'll use the basic connection string first.

        transaction(db) {
            // Create tables if they don't exist
            // To see SQL logs: addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
        // Comment out the seed data population after first run
        // Uncomment this block if you need to reset the seed data
        // runBlocking {
        //     populateInitialData()
        // }
    }

    // A helper function to execute database transactions
    // For Ktor, it's better to use newSuspendedTransaction from kotlinx-coroutines-exposed for non-blocking operations
    // For now, this synchronous version will work for initial setup.
    fun <T> dbQuery(block: () -> T): T =
        transaction { block() }
}
