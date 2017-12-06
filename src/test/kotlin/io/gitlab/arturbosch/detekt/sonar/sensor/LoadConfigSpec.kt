package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.rules.defaultYamlConfig
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.sonar.api.config.MapSettings
import java.io.File

/**
 * @author Artur Bosch
 */
class LoadConfigSpec : Spek({

	describe("try loading configuration file via property") {

		val base = File(javaClass.getResource("/configBase/config/detekt-config.yml").toURI()).parentFile
		assertThat(base).isNotNull()

		it("should match config on sub path level") {
			val settings = MapSettings().apply {
				setProperty(CONFIG_PATH_KEY, "detekt-config.yml")
			}
			val config = chooseConfig(base, settings)

			assertThat(config).isNotEqualTo(Config.empty)
		}

		it("should match config top path level") {
			val settings = MapSettings().apply {
				setProperty(CONFIG_PATH_KEY, "top-detekt-config.yml")
			}
			val config = chooseConfig(base, settings)

			assertThat(config).isNotEqualTo(Config.empty)
		}

		it("should default to default config") {
			val config = chooseConfig(base, MapSettings())

			assertThat(config == defaultYamlConfig)
		}
	}
})
