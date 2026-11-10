package com.ctfp.plugins

import com.asyncapi.kotlinasyncapi.context.service.AsyncApiExtension
import com.asyncapi.kotlinasyncapi.ktor.AsyncApiPlugin
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleMemoryCache.memoryCache
import com.ucasoft.ktor.simpleRedisCache.redisCache
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.host
import io.ktor.server.application.install
import io.ktor.server.application.port
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.hsts.HSTS
import io.ktor.server.plugins.httpsredirect.HttpsRedirect
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.header
import io.ktor.server.routing.contentType
import io.ktor.server.routing.header
import io.ktor.server.routing.host
import io.ktor.server.routing.options
import io.ktor.server.routing.path
import io.ktor.server.routing.port
import io.ktor.server.routing.routing
import kotlin.time.Duration.Companion.seconds

private const val MAX_RANGE_COUNT = 10

fun Application.configureHttp() {
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                else -> null
            }
        }
    }
    install(AsyncApiPlugin) {
        extension =
            AsyncApiExtension.builder {
                info {
                    title("Sample API")
                    version("1.0.0")
                }
            }
    }
    install(SimpleCache) {
        memoryCache {
            invalidateAt = 10.seconds
        }
    }
//    install(SimpleCache) {
//        redisCache {
//            invalidateAt = 10.seconds
//            host = "localhost"
//            port = 6379
//        }
//    }
    install(Compression)
    install(ConditionalHeaders)
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaders)
    install(HSTS) {
        includeSubDomains = true
    }
//    install(HttpsRedirect) {
//        // The port to redirect to. By default 443, the default HTTPS port.
//        sslPort = 443
//        // 301 Moved Permanently, or 302 Found redirect.
//        permanentRedirect = true
//    }
    routing {
        openAPI(path = "openapi") {
            /*
             Documentation source configuration goes here.

             This can be from file (documentation.yaml), or it can be served dynamically from your sources using the
             `describe {}` API on routes.  When `openApi` enabled in Gradle, these calls will be automatically injected
             based on your code and comments.
             */
        }
    }
    install(PartialContent) {
        // Maximum number of ranges that will be accepted from a HTTP request.
        // If the HTTP request specifies more ranges, they will all be merged into a single range.
        maxRangeCount = MAX_RANGE_COUNT
    }
    routing {
        swaggerUI(path = "swagger") {
            /*
             Documentation source configuration goes here.

             This can be from file (documentation.yaml), or it can be served dynamically from your sources using the
             `describe {}` API on routes.  When `openApi` enabled in Gradle, these calls will be automatically injected
             based on your code and comments.
             */
        }
    }
}
