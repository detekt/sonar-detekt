package io.gitlab.arturbosch.detekt.sonar.sensor

import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.tooling.api.spec.RulesSpec
import io.gitlab.arturbosch.detekt.sonar.foundation.BASELINE_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.CONFIG_PATH_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_DEFAULTS
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.logger
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.config.Configuration
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal fun createSpec(context: SensorContext): ProcessingSpec {
    val baseDir = context.fileSystem().baseDir().toPath()
    val settings = context.config()
    return createSpec(baseDir, settings)
}

internal fun createSpec(baseDir: Path, configuration: Configuration): ProcessingSpec {
    val configPath = tryFindDetektConfigurationFile(baseDir, configuration)
    val baselineFile = tryFindBaselineFile(baseDir, configuration)

    return ProcessingSpec {
        project {
            basePath = baseDir
            inputPaths = listOf(baseDir)
            excludes = getProjectExcludeFilters(configuration)
        }
        rules {
            activateAllRules = true // publish all; quality profiles will filter
            maxIssuePolicy = RulesSpec.MaxIssuePolicy.AllowAny
            autoCorrect = false // never change user files and conflict with sonar's reporting
        }
        config {
            useDefaultConfig = true
            configPaths = listOfNotNull(configPath)
        }
        baseline {
            path = baselineFile
        }
    }
}

internal fun getProjectExcludeFilters(configuration: Configuration): List<String> =
    configuration.get(PATH_FILTERS_KEY)
        .orElse(PATH_FILTERS_DEFAULTS)
        .splitToSequence(",", ";")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()

internal fun tryFindBaselineFile(baseDir: Path, config: Configuration): Path? {
    fun createBaselineFacade(path: Path): Path? {
        logger.info("Registered baseline path: $path")
        var baselinePath = path

        if (Files.notExists(baselinePath)) {
            baselinePath = baseDir.resolve(path)
        }

        if (Files.notExists(baselinePath)) {
            val parentFile = baseDir.parent
            if (parentFile != null) {
                baselinePath = parentFile.resolve(path)
            } else {
                return null
            }
        }
        return baselinePath
    }
    return config.get(BASELINE_KEY)
        .map { Paths.get(it) }
        .map(::createBaselineFacade)
        .orElse(null)
}

private val supportedYamlEndings = setOf(".yaml", ".yml")

internal fun tryFindDetektConfigurationFile(baseDir: Path, configuration: Configuration): Path? =
    configuration.get(CONFIG_PATH_KEY).map { path ->
        logger.info("Registered config path: $path")
        var configFile = Paths.get(path)

        if (Files.notExists(configFile) || supportedYamlEndings.any { path.toString().endsWith(it) }) {
            configFile = baseDir.resolve(path)
        }
        if (Files.notExists(configFile) || supportedYamlEndings.any { path.toString().endsWith(it) }) {
            val parentFile = baseDir.parent
            if (parentFile != null) {
                configFile = parentFile.resolve(path)
            } else {
                return@map null
            }
        }
        configFile
    }.orElse(null)
