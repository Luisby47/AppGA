// Main file should be in the root package

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import config.module

fun main() {
    System.setProperty("io.ktor.development", "true")
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
        watchPaths = listOf("target/classes") // Add this line
    ).start(wait = true)
}