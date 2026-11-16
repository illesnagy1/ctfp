plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "com.ctfp"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        optIn.add("io.ktor.utils.io.ExperimentalKtorApi")
    }
}

detekt {
    config.setFrom("config/detekt.yml")
    buildUponDefaultConfig = true
    baseline = file("config/detekt-baseline.xml")
}

kover {
    reports {
        filters {
            excludes {
                classes("GenerateMigrationKt", "com.ctfp.GenerateOpenApiKt")
            }
        }
    }
}

dependencies {
    implementation(ktorLibs.network.tls)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.autoHeadResponse)
    implementation(ktorLibs.server.cachingHeaders)
    implementation(ktorLibs.server.callId)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.conditionalHeaders)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.defaultHeaders)
    implementation(ktorLibs.server.forwardedHeader)
    implementation(ktorLibs.server.hsts)
    implementation(ktorLibs.server.httpRedirect)
    implementation(ktorLibs.server.metrics.micrometer)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.partialContent)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.routingOpenapi)
    implementation(ktorLibs.server.sse)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.swagger)
    implementation(ktorLibs.server.websockets)
    implementation(ktorLibs.server.cors)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.json)
    implementation(libs.exposed.migration.core)
    implementation(libs.exposed.migration.jdbc)
    implementation(libs.flaxoos.ktor.server.rateLimiting)
    implementation(libs.kotlinmailer.smtp)
    implementation(libs.h2database.h2)
    implementation(libs.koin.ktor)
    implementation(libs.koin.loggerSlf4j)
    implementation(libs.logback.classic)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.micrometer.registry.otlp)
    implementation(platform(libs.opentelemetry.instrumentation.bom.alpha))
    implementation(libs.opentelemetry.ktor)
    implementation(libs.opentelemetry.jdbc)
    implementation(libs.opentelemetry.logback.mdc)
    implementation(libs.opentelemetry.sdk.autoconfigure)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.openfolder.kotlinAsyncapiKtor)
    implementation(libs.postgresql)
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.ucasoft.ktorSimpleCache)
    implementation(libs.ucasoft.ktorSimpleMemoryCache)
    implementation(libs.ucasoft.ktorSimpleRedisCache)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.kotlinx.serialization.hocon)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}

tasks.test {
    environment(
        "UPLOADS_DIR",
        layout.buildDirectory
            .dir("test-uploads")
            .get()
            .asFile.absolutePath,
    )
    environment("UPLOADS_MAX_BYTES", "1048576")
    environment("SEED", "false")
}

tasks.register<JavaExec>("generateMigration") {
    group = "build"
    description = "Generate migration"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "GenerateMigrationKt"
}

tasks.register<JavaExec>("generateOpenApi") {
    group = "documentation"
    description = "Generates the OpenAPI documentation to openapi.json"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "com.ctfp.GenerateOpenApiKt"
    args(
        layout.projectDirectory
            .file("openapi.json")
            .asFile.path,
    )
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}
