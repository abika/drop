package org.abika

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import org.abika.model.FileModel
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.security.KeyStore

private fun getOrCreateKeyStore(path: File, keyStorePassword: String, certPassword: String): KeyStore {
    if (path.exists()) {
        val keyStoreInputStream = FileInputStream(path)
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray())
        return keyStore
    } else {
        val keyStore = buildKeyStore {
            certificate("sampleAlias") {
                password = certPassword
                domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
            }
        }
        keyStore.saveToFile(path, keyStorePassword)
        return keyStore
    }
}

private fun ApplicationEngine.Configuration.environmentConfig(keyStorePassword: String, certPassword: String, enableHTTP: Boolean) {
    val keyStoreFile = File("build/keystore.jks")
    val keyStore = getOrCreateKeyStore(keyStoreFile, keyStorePassword, certPassword)

    if (enableHTTP) {
        connector {
            port = 8080
        }
    }
    sslConnector(
        keyStore = keyStore,
        keyAlias = "sampleAlias",
        keyStorePassword = { keyStorePassword.toCharArray() },
        privateKeyPassword = { certPassword.toCharArray() }) {
        port = 8443
        keyStorePath = keyStoreFile
    }
}

private class Server : CliktCommand() {
    val fileDirectory: String by argument(help = "file folder that is published")

    val fileModel: FileModel by lazy { FileModel(Paths.get(fileDirectory)) }

    private val respondByIndexBody: suspend RoutingContext.() -> Unit = {
        call.respond(
            ThymeleafContent(
                "index",
                mapOf("files" to fileModel.items())
            )
        )
    }

    override fun run() {
        embeddedServer(Netty,
            environment = applicationEnvironment { },
            configure = { environmentConfig("123456", "foobar", false) }) {
            println("Starting server with directory '${fileDirectory}' and waiting...")

            install(Thymeleaf) {
                setTemplateResolver(ClassLoaderTemplateResolver().apply {
                    prefix = "templates/thymeleaf/"
                    suffix = ".html"
                    characterEncoding = "utf-8"
                })
            }

            routing {
                get("/", respondByIndexBody)

                post("/upload") {
                    println("Upload called")

                    call.receiveMultipart().forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                val filename = part.originalFileName as String
                                val fileBytes = part.provider().readRemaining().readByteArray()
                                fileModel.save(filename, fileBytes)
                            }

                            else -> {
                                println("Unexpected multipart data: $part")
                            }
                        }
                        part.dispose()
                    }

                    call.respondRedirect("/", permanent = false)
                }
            }
        }.start(wait = true)
    }
}

fun main(args: Array<String>) {
    Server().main(args)
}
