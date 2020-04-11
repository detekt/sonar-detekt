package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.DetektFacade
import io.gitlab.arturbosch.detekt.sonar.foundation.DETEKT_SENSOR
import io.gitlab.arturbosch.detekt.sonar.foundation.LANGUAGE_KEY
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor

class DetektSensor : Sensor {

    override fun describe(descriptor: SensorDescriptor) {
        descriptor.name(DETEKT_SENSOR).onlyOnLanguage(LANGUAGE_KEY)
    }

    override fun execute(context: SensorContext) {
        val settings = createProcessingSettings(context)
        val facade = DetektFacade.create(settings)
        val result = facade.run()
        IssueReporter(result, context).run()
    }
}
