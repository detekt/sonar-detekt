package io.gitlab.arturbosch.detekt.sonar

import io.gitlab.arturbosch.detekt.sonar.foundation.KotlinLanguage
import io.gitlab.arturbosch.detekt.sonar.foundation.propertyDefinitions
import io.gitlab.arturbosch.detekt.sonar.profiles.KotlinProfile
import io.gitlab.arturbosch.detekt.sonar.rules.DetektRulesDefinition
import io.gitlab.arturbosch.detekt.sonar.sensor.DetektMetrics
import io.gitlab.arturbosch.detekt.sonar.sensor.DetektSensor
import org.sonar.api.Plugin

/**
 * @author Artur Bosch
 */
class DetektPlugin : Plugin {

	override fun define(context: Plugin.Context) {
		context.addExtensions(listOf(
				KotlinLanguage::class.java,
				KotlinProfile::class.java,
				DetektSensor::class.java,
				DetektRulesDefinition::class.java,
				DetektMetrics::class.java))
		context.addExtensions(propertyDefinitions)
	}

}
