package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.cli.CliArgs
import io.gitlab.arturbosch.detekt.cli.createFilters
import io.gitlab.arturbosch.detekt.cli.loadConfiguration
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_DEFAULTS
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.logger
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.config.Configuration
import java.io.File

fun createProcessingSettings(context: SensorContext): ProcessingSettings {
    val baseDir = context.fileSystem().baseDir()
    val settings = context.config()
    val filters = CliArgs {
        excludes = settings.get(PATH_FILTERS_KEY).orElse(PATH_FILTERS_DEFAULTS)
    }.createFilters()
    val config = chooseConfig(baseDir, settings)
    return ProcessingSettings(
        inputPaths = listOf(baseDir.toPath()),
        config = config,
        pathFilters = filters,
        outPrinter = System.out,
        errPrinter = System.err
    )
}

internal fun chooseConfig(baseDir: File, configuration: Configuration): Config {
    val externalConfigPath = tryFindDetektConfigurationFile(configuration, baseDir)

    val possibleParseArguments = CliArgs().apply {
        config = externalConfigPath?.path
        failFast = true // always use FailFast config to activate all detekt rules
        autoCorrect = false // never change user files and conflict with sonar's reporting
    }

    return possibleParseArguments.loadConfiguration()
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
