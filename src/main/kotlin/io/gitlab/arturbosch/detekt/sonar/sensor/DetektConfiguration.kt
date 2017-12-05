package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.cli.Args
import io.gitlab.arturbosch.detekt.cli.SEPARATOR_COMMA
import io.gitlab.arturbosch.detekt.cli.SEPARATOR_SEMICOLON
import io.gitlab.arturbosch.detekt.cli.loadConfiguration
import io.gitlab.arturbosch.detekt.core.PathFilter
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_RESOURCE_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.LOG
import io.gitlab.arturbosch.detekt.sonar.foundation.NoAutoCorrectConfig
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_DEFAULTS
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import io.gitlab.arturbosch.detekt.sonar.rules.DEFAULT_YAML_CONFIG
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.config.Settings
import java.io.File

/**
 * @author Artur Bosch
 */
fun createProcessingSettings(context: SensorContext): ProcessingSettings {
	val baseDir = context.fileSystem().baseDir()
	val settings = context.settings()
	val pathFiltersString = settings.getString(PATH_FILTERS_KEY) ?: PATH_FILTERS_DEFAULTS
	val filters = pathFiltersString.split(SEPARATOR_SEMICOLON, SEPARATOR_COMMA).map { PathFilter(it) }
	val config = chooseConfig(baseDir, settings)
	return ProcessingSettings(baseDir.toPath(), NoAutoCorrectConfig(config), filters)
}

internal fun chooseConfig(baseDir: File, settings: Settings): Config {
	val externalConfigPath = tryFindDetektConfigurationFile(settings, baseDir)

	val internalConfigResource = settings.getString(CONFIG_RESOURCE_KEY)
			?.let { if (it.isBlank()) null else it }

	val possibleParseArguments = Args().apply {
		config = externalConfigPath?.path
		configResource = internalConfigResource
	}

	return possibleParseArguments.loadConfiguration().let { bestConfigMatch ->
		if (bestConfigMatch == Config.empty) {
			LOG.info("No detekt yaml configuration file found, using the default configuration.")
			DEFAULT_YAML_CONFIG
		} else {
			bestConfigMatch
		}
	}
}

private fun tryFindDetektConfigurationFile(settings: Settings, baseDir: File): File? {
	return settings.getString(CONFIG_PATH_KEY)?.let { path ->
		LOG.info("Registered config path: $path")
		var configFile = File(path)

		if (!configFile.exists() || configFile.endsWith(".yaml")) {
			configFile = File(baseDir.path, path)
		}
		if (!configFile.exists() || configFile.endsWith(".yaml")) {
			val parentFile = baseDir.parentFile
			if (parentFile != null) {
				configFile = File(parentFile.path, path)
			} else {
				return null
			}
		}
		configFile
	}
}
