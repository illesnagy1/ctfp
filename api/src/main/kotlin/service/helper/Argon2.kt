package com.ctfp.service.helper

import java.util.Base64

private val encoder = Base64.getEncoder().withoutPadding()
private val decoder = Base64.getDecoder()

private val pattern =
    $$"""^\$argon2id\$v=19\$m=(?<memory>\d+),t=(?<iterations>\d+),p=(?<parallelism>\d+)\$(?<salt>[A-Za-z0-9+/]+)\$(?<hash>[A-Za-z0-9+/]+)$""".toRegex()

/**
 * Argon2id v1.3 hash, encoded as a PHC string:
 * `$argon2id$v=19$m=<KiB>,t=<iterations>,p=<parallelism>$<salt>$<hash>`
 */
class Argon2id(
    val memoryKib: Int,
    val iterations: Int,
    val parallelism: Int,
    val salt: ByteArray,
    val hash: ByteArray,
) {
    init {
        require(parallelism > 0) { "Invalid parallelism: $parallelism" }
        require(iterations > 0) { "Invalid iterations: $iterations" }
        require(memoryKib >= 8 * parallelism) { "Invalid memory cost: $memoryKib" }
    }

    fun encode() =
        $$"$argon2id$v=19$m=$$memoryKib,t=$$iterations,p=$$parallelism" +
                $$"$$${encoder.encodeToString(salt)}$$${encoder.encodeToString(hash)}"
}

fun String.toArgon2id(): Argon2id {
    val groups = requireNotNull(pattern.matchEntire(this)) { "Invalid Argon2id hash format" }.groups
    fun group(name: String) = groups[name]!!.value
    fun int(name: String) = requireNotNull(group(name).toIntOrNull()) { "Invalid Argon2id $name" }

    return Argon2id(
        memoryKib = int("memory"),
        iterations = int("iterations"),
        parallelism = int("parallelism"),
        salt = decoder.decode(group("salt")),
        hash = decoder.decode(group("hash")),
    )
}
