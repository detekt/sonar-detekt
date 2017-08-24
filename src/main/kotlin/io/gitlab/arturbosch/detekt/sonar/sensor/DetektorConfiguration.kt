package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.cli.Args
import io.gitlab.arturbosch.detekt.cli.SEPARATOR_COMMA
import io.gitlab.arturbosch.detekt.cli.SEPARATOR_SEMICOLON
import io.gitlab.arturbosch.detekt.cli.loadConfiguration
import io.gitlab.arturbosch.detekt.core.DetektFacade
import io.gitlab.arturbosch.detekt.core.Detektor
import io.gitlab.arturbosch.detekt.core.FileProcessorLocator
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
class DetektorConfiguration(context: SensorContext) {

	private val config: Config
	private val settings: Settings
	private val baseDir: File

	init {
		val fileSystem = context.fileSystem()
		baseDir = fileSystem.baseDir()
		settings = context.settings()
		config = chooseConfig(baseDir, settings)
	}

	/**
	 * Provides an instance of [Detektor] to be used for whole project analysis.
	 */
	fun configureDetektor(): Pair<Detektor, FileProcessorLocator> {
		val pathFiltersString = settings.getString(PATH_FILTERS_KEY) ?: PATH_FILTERS_DEFAULTS
		val filters = pathFiltersString.split(SEPARATOR_SEMICOLON, SEPARATOR_COMMA).map { PathFilter(it) }
		val processingSettings = ProcessingSettings(baseDir.toPath(), NoAutoCorrectConfig(config), filters)
		val fileProcessorLocator = FileProcessorLocator(processingSettings)

		return DetektFacade.instance(processingSettings).to(fileProcessorLocator)
	}

	private fun chooseConfig(baseDir: File, settings: Settings): Config {
		val externalConfigPath = settings.getString(CONFIG_PATH_KEY)?.let { configPath ->
			LOG.info("Registered config path: $configPath")
			val configFile = File(configPath)
			if (!configFile.isAbsolute) { // TODO find out how to resolve always to root path, not module path
				val resolved = baseDir.resolve(configPath)
				LOG.info("Relative path detected. Resolving to project dir: $resolved")
				resolved
			} else {
				configFile
			}
		}

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

}
