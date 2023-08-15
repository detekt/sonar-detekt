package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_DEFAULTS
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.sonar.api.config.internal.MapSettings
import java.nio.file.Paths

internal class DetektConfigurationTest {

    @Nested
    internal class `try loading configuration file via property` {

        val base = Paths.get(javaClass.getResource("/configBase/config/detekt-config.yml").toURI()).parent

        @Test
        fun `should match config on sub path level`() {
            val settings = MapSettings().apply {
                setProperty(CONFIG_PATH_KEY, "detekt-config.yml")
            }
            val config = tryFindDetektConfigurationFile(base, settings.asConfig())

            assertThat(config).isNotNull()
        }

        @Test
        fun `should match config top path level`() {
            val settings = MapSettings().apply {
                setProperty(CONFIG_PATH_KEY, "top-detekt-config.yml")
            }

            val config = tryFindDetektConfigurationFile(base, settings.asConfig())

            assertThat(config).isNotNull()
        }

        @Test
        fun `returns null on no property provided`() {
            val config = tryFindDetektConfigurationFile(base, MapSettings().asConfig())

            assertThat(config).isNull()
        }
    }

    @Nested
    internal class `exclude filters"` {

        @Test
        fun `transforms filters to a list`() {
            val settings = MapSettings()
            settings.setProperty(PATH_FILTERS_KEY, PATH_FILTERS_DEFAULTS)

            val filters = getProjectExcludeFilters(settings.asConfig())

            assertThat(filters).contains("**/resources/**", "**/build/**", "**/target/**")
        }

        @Test
        fun `defaults to resources, target and build as exclude filters`() {
            val filters = getProjectExcludeFilters(MapSettings().asConfig())

            assertThat(filters).contains("**/resources/**", "**/build/**", "**/target/**")
        }
    }
}
