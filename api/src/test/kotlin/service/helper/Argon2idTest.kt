package com.ctfp.service.helper

import kotlin.test.*

class Argon2idTest {
    private companion object {
        // Reference vector from the argon2 reference implementation (test.c): "password" / "somesalt"
        const val REFERENCE = $$"$argon2id$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc"
    }

    @Test
    fun `parses reference hash`() {
        val parsed = REFERENCE.toArgon2id()

        assertEquals(65536, parsed.memoryKib)
        assertEquals(2, parsed.iterations)
        assertEquals(1, parsed.parallelism)
        assertContentEquals("somesalt".toByteArray(), parsed.salt)
        assertEquals("09316115d5cf24ed5a15a31a3ba326e5cf32edc24702987c02b6566f61913cf7", parsed.hash.toHexString())
    }

    @Test
    fun `encode round trips`() {
        assertEquals(REFERENCE, REFERENCE.toArgon2id().encode())
    }

    @Test
    fun `encode omits base64 padding`() {
        val encoded = Argon2id(64, 1, 1, ByteArray(16), ByteArray(32)).encode()

        assertFalse('=' in encoded)
        assertEquals(encoded, encoded.toArgon2id().encode())
    }

    @Test
    fun `rejects malformed strings`() {
        val invalid = listOf(
            "",
            "argon2id",
            $$"$argon2i$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2d$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=16$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=19$t=2,m=65536,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=19$m=65536,t=2$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=19$m=abc,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=19$m=-1,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=19$m=65536,t=2,p=1$c29tZXNhbHQ",
            REFERENCE.replace("\$CTFh", "\$\$CTFh"),
            $$"$argon2id$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc$extra",
            $$"$argon2id$v=19$m=65536,t=2,p=1$c29tZXNhbHQ=$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$"$argon2id$v=19$m=65536,t=2,p=1$c29t-XNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
            $$" $argon2id$v=19$m=65536,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc",
        )

        invalid.forEach { assertFailsWith<IllegalArgumentException>(it) { it.toArgon2id() } }
    }

    @Test
    fun `rejects parameters overflowing int`() {
        assertFailsWith<IllegalArgumentException> {
            $$"$argon2id$v=19$m=99999999999,t=2,p=1$c29tZXNhbHQ$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc".toArgon2id()
        }
    }

    @Test
    fun `rejects invalid base64 length`() {
        assertFailsWith<IllegalArgumentException> {
            $$"$argon2id$v=19$m=65536,t=2,p=1$A$CTFhFdXPJO1aFaMaO6Mm5c8y7cJHAph8ArZWb2GRPPc".toArgon2id()
        }
    }

    @Test
    fun `rejects out of range parameters`() {
        assertFailsWith<IllegalArgumentException> { $$"$argon2id$v=19$m=0,t=2,p=1$c29tZXNhbHQ$CTFh".toArgon2id() }
        assertFailsWith<IllegalArgumentException> { $$"$argon2id$v=19$m=64,t=0,p=1$c29tZXNhbHQ$CTFh".toArgon2id() }
        assertFailsWith<IllegalArgumentException> { $$"$argon2id$v=19$m=64,t=2,p=0$c29tZXNhbHQ$CTFh".toArgon2id() }
        // memory must be at least 8 KiB per lane
        assertFailsWith<IllegalArgumentException> { $$"$argon2id$v=19$m=31,t=2,p=4$c29tZXNhbHQ$CTFh".toArgon2id() }
    }

    @Test
    fun `constructor validates parameters`() {
        assertFailsWith<IllegalArgumentException> { Argon2id(64, 0, 1, ByteArray(16), ByteArray(32)) }
        assertFailsWith<IllegalArgumentException> { Argon2id(64, 1, 0, ByteArray(16), ByteArray(32)) }
        assertFailsWith<IllegalArgumentException> { Argon2id(7, 1, 1, ByteArray(16), ByteArray(32)) }
        Argon2id(8, 1, 1, ByteArray(16), ByteArray(32))
    }
}
