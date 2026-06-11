package dev.alex.afktoghost

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AfkGhostConfigTest {
    @Test
    fun parsesCompleteConfig() {
        val config = AfkGhostConfig.parseJson(
            """
            {
              "timeoutSeconds": 120,
              "debug": false,
              "invisibility": true,
              "invulnerable": false,
              "actionbar": true
            }
            """.trimIndent()
        ).getOrThrow()

        assertEquals(120L, config.timeoutSeconds)
        assertFalse(config.debug)
        assertTrue(config.invisibility)
        assertFalse(config.invulnerable)
        assertTrue(config.actionbar)
    }

    @Test
    fun missingKeysUseDefaults() {
        val config = AfkGhostConfig.parseJson(
            """
            {
              "timeoutSeconds": 60
            }
            """.trimIndent()
        ).getOrThrow()

        assertEquals(60L, config.timeoutSeconds)
        assertTrue(config.debug)
        assertTrue(config.invisibility)
        assertTrue(config.invulnerable)
        assertTrue(config.actionbar)
    }

    @Test
    fun rejectsUnknownKeys() {
        assertTrue(
            AfkGhostConfig.parseJson(
                """
                {
                  "timeoutSeconds": 60,
                  "unknown": true
                }
                """.trimIndent()
            ).isFailure
        )
    }

    @Test
    fun rejectsInvalidTypes() {
        assertTrue(
            AfkGhostConfig.parseJson(
                """
                {
                  "timeoutSeconds": "60"
                }
                """.trimIndent()
            ).isFailure
        )
        assertTrue(
            AfkGhostConfig.parseJson(
                """
                {
                  "debug": "false"
                }
                """.trimIndent()
            ).isFailure
        )
    }

    @Test
    fun rejectsInvalidRanges() {
        assertTrue(
            AfkGhostConfig.parseJson(
                """
                {
                  "timeoutSeconds": 0
                }
                """.trimIndent()
            ).isFailure
        )
        assertTrue(
            AfkGhostConfig.parseJson(
                """
                {
                  "timeoutSeconds": 1.5
                }
                """.trimIndent()
            ).isFailure
        )
    }

    @Test
    fun commandValueParsingDoesNotMutateOriginalConfigOnFailure() {
        val original = AfkGhostConfig(timeoutSeconds = 300, debug = true, invisibility = true, invulnerable = true, actionbar = true)

        assertTrue(original.withParsedValue("timeoutSeconds", "10").isSuccess)
        assertTrue(original.withParsedValue("timeoutSeconds", "0").isFailure)
        assertTrue(original.withParsedValue("debug", "maybe").isFailure)
        assertTrue(original.withParsedValue("missing", "true").isFailure)
        assertEquals(300L, original.timeoutSeconds)
        assertTrue(original.debug)
    }
}
