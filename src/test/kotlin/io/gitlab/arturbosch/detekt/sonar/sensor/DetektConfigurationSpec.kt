package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_DEFAULTS
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import org.assertj.core.api.Assertions.assertThat

import org.sonar.api.config.internal.MapSettings
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Paths

internal class DetektConfigurationSpec : Spek({

    describe("try loading configuration file via property") {

        val base = Paths.get(javaClass.getResource("/configBase/config/detekt-config.yml").toURI()).parent

        it("should match config on sub path level") {
            val settings = MapSettings().apply {
                setProperty(CONFIG_PATH_KEY, "detekt-config.yml")
            }
            val config = tryFindDetektConfigurationFile(base, settings.asConfig())

            assertThat(config).isNotNull()
        }

        it("should match config top path level") {
            val settings = MapSettings().apply {
                setProperty(CONFIG_PATH_KEY, "top-detekt-config.yml")
            }

            val config = tryFindDetektConfigurationFile(base, settings.asConfig())

            assertThat(config).isNotNull()
        }

        it("returns null on no property provided") {
            val config = tryFindDetektConfigurationFile(base, MapSettings().asConfig())

            assertThat(config).isNull()
        }
    }

    describe("exclude filters") {

        it("transforms filters to a list") {
            val settings = MapSettings()
            settings.setProperty(PATH_FILTERS_KEY, PATH_FILTERS_DEFAULTS)

            val filters = getProjectExcludeFilters(settings.asConfig())

            assertThat(filters).contains("**/resources/**", "**/build/**", "**/target/**")
        }

        it("defaults to resources, target and build as exclude filters") {
            val filters = getProjectExcludeFilters(MapSettings().asConfig())

            assertThat(filters).contains("**/resources/**", "**/build/**", "**/target/**")
        }
    }
})
