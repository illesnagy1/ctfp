package com.ctfp.service

import com.ctfp.dto.Settings
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.hocon.encodeToConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.writeText

/**
 * Keeps the [Settings] in memory and persists them as HOCON to [file]. A missing file means defaults; it is created
 * on the first update. Reads are served from memory, so edits made to the file by hand need a restart.
 */
@OptIn(ExperimentalSerializationApi::class)
class SettingsService(private val file: Path) {
    private val hocon = Hocon { encodeDefaults = true }
    private val mutex = Mutex()

    @Volatile
    var current: Settings = load()
        private set

    fun getSettings(): Settings = current

    suspend fun updateSettings(settings: Settings): Settings = mutex.withLock {
        validate(settings)
        withContext(Dispatchers.IO) { write(settings) }
        current = settings
        settings
    }

    private fun load(): Settings =
        if (file.exists()) hocon.decodeFromConfig(ConfigFactory.parseFile(file.toFile()).resolve())
        else Settings()

    /** Writes to a sibling temp file and moves it into place, so a crash never leaves a half-written file. */
    private fun write(settings: Settings) {
        file.toAbsolutePath().parent?.createDirectories()
        val rendered = hocon.encodeToConfig(settings).root()
            .render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false))
        val tmp = file.resolveSibling("${file.name}.tmp")
        tmp.writeText(rendered)
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    private fun validate(settings: Settings) {
        if (settings.ctfName.isBlank()) {
            throw ApiException(HttpStatusCode.BadRequest, "ctfName must not be blank")
        }
        if (settings.maxTeamSize < 1) {
            throw ApiException(HttpStatusCode.BadRequest, "maxTeamSize must be at least 1")
        }
    }
}
