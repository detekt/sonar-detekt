package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.cli.CliArgs
import io.gitlab.arturbosch.detekt.cli.SEPARATOR_COMMA
import io.gitlab.arturbosch.detekt.cli.SEPARATOR_SEMICOLON
import io.gitlab.arturbosch.detekt.cli.loadConfiguration
import io.gitlab.arturbosch.detekt.core.PathFilter
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.NoAutoCorrectConfig
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_DEFAULTS
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.logger
import io.gitlab.arturbosch.detekt.sonar.rules.defaultYamlConfig
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.config.Configuration
import java.io.File

fun createProcessingSettings(context: SensorContext): ProcessingSettings {
    val baseDir = context.fileSystem().baseDir()
    val settings = context.config()
    val pathFiltersString = settings.get(PATH_FILTERS_KEY).orElse(PATH_FILTERS_DEFAULTS)
    val filters = pathFiltersString.split(SEPARATOR_SEMICOLON, SEPARATOR_COMMA).map { PathFilter(it) }
    val config = chooseConfig(baseDir, settings)
    return ProcessingSettings(baseDir.toPath(), NoAutoCorrectConfig(config), filters)
}

internal fun chooseConfig(baseDir: File, configuration: Configuration): Config {
    val externalConfigPath = tryFindDetektConfigurationFile(configuration, baseDir)

    val possibleParseArguments = CliArgs().apply {
        config = externalConfigPath?.path
    }

    return possibleParseArguments.loadConfiguration().let { bestConfigMatch ->
        if (bestConfigMatch == Config.empty) {
            logger.info("No detekt yaml configuration file found, using the default configuration.")
            defaultYamlConfig
        } else {
            bestConfigMatch
        }
    }
}

private fun tryFindDetektConfigurationFile(configuration: Configuration, baseDir: File): File? {
    return configuration.get(CONFIG_PATH_KEY).map { path ->
        logger.info("Registered config path: $path")
        var configFile = File(path)

        if (!configFile.exists() || configFile.endsWith(".yaml")) {
            configFile = File(baseDir.path, path)
        }
        if (!configFile.exists() || configFile.endsWith(".yaml")) {
            val parentFile = baseDir.parentFile
            if (parentFile != null) {
                configFile = File(parentFile.path, path)
            } else {
                return@map null
            }
        }
        configFile
    }.orElse(null)
}
