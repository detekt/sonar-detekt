package io.gitlab.arturbosch.detekt.sonar

import io.gitlab.arturbosch.detekt.sonar.foundation.DetektProfile
import io.gitlab.arturbosch.detekt.sonar.foundation.propertyDefinitions
import io.gitlab.arturbosch.detekt.sonar.rules.DetektRulesDefinition
import io.gitlab.arturbosch.detekt.sonar.sensor.DetektSensor
import org.sonar.api.Plugin

class DetektPlugin : Plugin {

    override fun define(context: Plugin.Context) {
        context.addExtensions(listOf(
            DetektProfile::class.java,
            DetektSensor::class.java,
            DetektRulesDefinition::class.java
        ))
        context.addExtensions(propertyDefinitions)
    }
}
