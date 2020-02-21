package io.gitlab.arturbosch.detekt.sonar.foundation

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.YamlConfig
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.*
import java.net.URL
import org.assertj.core.api.Assertions.assertThat

class NoAutoCorrectConfigTest : Spek({

    describe("a configuration") {

        context("with autoCorrect on 1st level") {
            val config = NoAutoCorrectConfig(
                    createConfig("""
                        autoCorrect: true
                    """.trimIndent())
            )

            it("should be false") {
                assertThat(config.valueOrDefault("autoCorrect", true)).isEqualTo(false)
            }
        }

        context("with autoCorrect on 2d level") {
            val config = NoAutoCorrectConfig(
                    createConfig("""
                        secondLevel:
                          autoCorrect: true
                    """.trimIndent())
            )

            it("should be false") {
                val secondLevelConfig = config.subConfig("secondLevel")
                assertThat(secondLevelConfig.valueOrDefault("autoCorrect", true)).isEqualTo(false)
            }
        }

        context("with autoCorrect on 3rd level") {
            val config = NoAutoCorrectConfig(
                    createConfig("""
                        secondLevel:
                          thirdLevel:
                            autoCorrect: true
                    """.trimIndent())
            )

            it("should be false") {
                val thirdLevelConfig = config.subConfig("secondLevel").subConfig("thirdLevel")
                assertThat(thirdLevelConfig.valueOrDefault("autoCorrect", true)).isEqualTo(false)
            }
        }
    }
})

private fun createConfig(yamlInput: String): Config {
    val yamlInputStream = ByteArrayInputStream(yamlInput.toByteArray(Charsets.UTF_8))
    val url = mockk<URL>()
    every { url.openStream() } returns yamlInputStream
    return YamlConfig.loadResource(url)
}
