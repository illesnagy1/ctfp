package com.ctfp.service.helper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Argon2id 1.3 password hashing, encoded as a PHC string:
 * `$argon2id$v=19$m=<KiB>,t=<iterations>,p=<parallelism>$<salt>$<hash>`
 *
 * Defaults follow the OWASP recommendation (m=19 MiB, t=2, p=1).
 * https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
 */
class PasswordHasher(
    private val memoryKib: Int = 19 * 1024,
    private val iterations: Int = 2,
    private val parallelism: Int = 1,
) {
    private companion object {
        const val SALT_LENGTH = 16
        const val HASH_LENGTH = 32
    }
    private val random = SecureRandom()

    suspend fun hash(password: String): String = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LENGTH).also(random::nextBytes)
        val hash = derive(password, salt, memoryKib, iterations, parallelism, HASH_LENGTH)
        hash.encode()
    }

    suspend fun verify(password: String, encoded: String): Boolean = withContext(Dispatchers.Default) {
        runCatching {
            val hash = encoded.toArgon2id()
            val actual = derive(password, hash.salt, hash.memoryKib, hash.iterations, hash.parallelism, hash.hash.size)

            MessageDigest.isEqual(hash.hash, actual.hash)
        }.getOrDefault(false)
    }

    private fun derive(password: String, salt: ByteArray, memoryKib: Int, iterations: Int, parallelism: Int, length: Int): Argon2id {
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withMemoryAsKB(memoryKib)
            .withIterations(iterations)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()
        val generator = Argon2BytesGenerator().apply { init(params) }
        val hash = ByteArray(length).also { generator.generateBytes(password.toCharArray(), it) }
        return Argon2id(
            memoryKib = params.memory,
            iterations = params.iterations,
            parallelism = params.lanes,
            salt = params.salt,
            hash = hash,
        )
    }
}
