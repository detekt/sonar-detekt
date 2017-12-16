package io.gitlab.arturbosch.detekt.sonar.foundation

import org.sonar.api.config.PropertyDefinition
import org.sonar.api.resources.Qualifiers

/**
 * @author Artur Bosch
 */

const val CONFIG_PATH_NAME = "Detekt yaml configuration path"
const val CONFIG_PATH_KEY = "detekt.sonar.kotlin.config.path"
const val CONFIG_PATH_DESCRIPTION = "Path to the detekt yaml config." +
		" Path may be absolute or relative to the project base directory."
const val CONFIG_PATH_DEFAULT = ""

const val PATH_FILTERS_NAME = "Detekt path filters"
const val PATH_FILTERS_KEY = "detekt.sonar.kotlin.filters"
const val PATH_FILTERS_DESCRIPTIONS = "Regex based path filters eg. '.*/test/.*'. " +
		"All paths like '/my/custom/test/path' will be filtered."
const val PATH_FILTERS_DEFAULTS = ".*/test/.*,.*/resources/.*,.*/build/.*,.*/target/.*"

const val BASELINE_NAME = "Detekt baseline configuration path"
const val BASELINE_KEY = "detekt.sonar.kotlin.baseline.path"
const val BASELINE_DESCRIPTION = "Path to the detekt baseline xml file. " +
		"A baseline file is used to white- or blacklist code smells."
const val BASELINE_DEFAULT = ""

val propertyDefinitions = listOf<PropertyDefinition>(
		PropertyDefinition.builder(CONFIG_PATH_KEY)
				.name(CONFIG_PATH_NAME)
				.defaultValue(CONFIG_PATH_DEFAULT)
				.description(CONFIG_PATH_DESCRIPTION)
				.onQualifiers(listOf(Qualifiers.PROJECT))
				.build(),
		PropertyDefinition.builder(PATH_FILTERS_KEY)
				.name(PATH_FILTERS_NAME)
				.defaultValue(PATH_FILTERS_DEFAULTS)
				.description(PATH_FILTERS_DESCRIPTIONS)
				.onQualifiers(listOf(Qualifiers.PROJECT))
				.build(),
		PropertyDefinition.builder(BASELINE_KEY)
				.name(BASELINE_NAME)
				.defaultValue(BASELINE_DEFAULT)
				.description(BASELINE_DESCRIPTION)
				.onQualifiers(listOf(Qualifiers.PROJECT))
				.build()
)
