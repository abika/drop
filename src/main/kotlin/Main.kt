package org.abika

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import org.abika.model.FileModel
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.nio.file.Paths

class Server : CliktCommand() {
    val fileDirectory: String by argument(help="file folder that is published")

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

        println("Starting server with directory '${fileDirectory}' and waiting...")
        embeddedServer(Netty, 8080) {
            install(Thymeleaf) {
                setTemplateResolver(ClassLoaderTemplateResolver().apply {
                    prefix = "templates/thymeleaf/"
                    suffix = ".html"
                    characterEncoding = "utf-8"
                })
            }

            routing {
//            get("/") {
//                call.respondText("Hello Foobar!"
//                    , ContentType.Text.Html)
//            }
                get("/", respondByIndexBody)

                post("upload") {
                    println("Upload called")
                    val multiPartData = call.receiveMultipart()
                    val formContent = call.receiveParameters()
                    val file = formContent["file"]

                    println("file=$file data=$multiPartData")

                    try {
                        respondByIndexBody()
                    } catch (ex: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest)
                    } catch (ex: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }
        }.start(wait = true)
    }
}

fun main(args: Array<String>) {
    Server().main(args)
}
