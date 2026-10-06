package com.ctfp.service.helper

import kotlinx.coroutines.runBlocking
import kotlin.test.*

class PasswordHasherTest {
    private companion object {
        // Reference vector from the argon2 reference implementation (test.c): "password" / "somesalt"
        const val REFERENCE = $$"$argon2id$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc"
    }

    // Cheap parameters so tests stay fast
    private val hasher = PasswordHasher(memoryKib = 64, iterations = 1, parallelism = 1)

    @Test
    fun `hash encodes configured parameters`() = runBlocking {
        val parsed = hasher.hash("hunter2").toArgon2id()

        assertEquals(64, parsed.memoryKib)
        assertEquals(1, parsed.iterations)
        assertEquals(1, parsed.parallelism)
        assertEquals(16, parsed.salt.size)
        assertEquals(32, parsed.hash.size)
    }

    @Test
    fun `default parameters follow OWASP`() = runBlocking {
        val parsed = PasswordHasher().hash("hunter2").toArgon2id()

        assertEquals(19 * 1024, parsed.memoryKib)
        assertEquals(2, parsed.iterations)
        assertEquals(1, parsed.parallelism)
    }

    @Test
    fun `verify accepts correct password`() = runBlocking {
        assertTrue(hasher.verify("hunter2", hasher.hash("hunter2")))
    }

    @Test
    fun `verify rejects wrong password`() = runBlocking {
        val encoded = hasher.hash("hunter2")

        assertFalse(hasher.verify("hunter3", encoded))
        assertFalse(hasher.verify("", encoded))
        assertFalse(hasher.verify("Hunter2", encoded))
    }

    @Test
    fun `same password gets unique salt`() = runBlocking {
        val first = hasher.hash("hunter2")
        val second = hasher.hash("hunter2")

        assertNotEquals(first, second)
        assertTrue(hasher.verify("hunter2", first))
        assertTrue(hasher.verify("hunter2", second))
    }

    @Test
    fun `handles unicode and empty passwords`() = runBlocking {
        listOf("", "pässwörd", "密码", "🔑🔒").forEach {
            assertTrue(hasher.verify(it, hasher.hash(it)), it)
        }
        assertFalse(hasher.verify("passwörd", hasher.hash("passwörd".replace("ö", "ö"))))
    }

    @Test
    fun `verify matches reference vector`() = runBlocking {
        assertTrue(hasher.verify("password", REFERENCE))
        assertFalse(hasher.verify("passwore", REFERENCE))
    }

    @Test
    fun `verify uses parameters from stored hash`() = runBlocking {
        val encoded = PasswordHasher(memoryKib = 128, iterations = 2, parallelism = 2).hash("hunter2")

        assertTrue(hasher.verify("hunter2", encoded))
    }

    @Test
    fun `verify rejects tampered hash`() = runBlocking {
        val parsed = hasher.hash("hunter2").toArgon2id()
        val tampered = Argon2id(
            parsed.memoryKib,
            parsed.iterations + 1,
            parsed.parallelism,
            parsed.salt,
            parsed.hash,
        ).encode()

        assertFalse(hasher.verify("hunter2", tampered))
    }

    @Test
    fun `verify returns false on malformed hash`() = runBlocking {
        listOf("", "not a hash", $$"$argon2i$v=19$m=64,t=1,p=1$c29tZXNhbHQ$CTFh").forEach {
            assertFalse(hasher.verify("hunter2", it), it)
        }
    }
}
