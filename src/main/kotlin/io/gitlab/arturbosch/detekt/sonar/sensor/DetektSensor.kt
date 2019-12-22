package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.DetektFacade
import io.gitlab.arturbosch.detekt.core.FileProcessorLocator
import io.gitlab.arturbosch.detekt.core.RuleSetLocator
import io.gitlab.arturbosch.detekt.sonar.foundation.DETEKT_SENSOR
import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor

class DetektSensor : Sensor {

    override fun describe(descriptor: SensorDescriptor) {
        descriptor.name(DETEKT_SENSOR).onlyOnLanguage(KEY)
    }

    override fun execute(context: SensorContext) {
        val settings = createProcessingSettings(context)
        val providers = RuleSetLocator(settings).load()
        val ktFilesListener = ReturnKtFilesAnalysisEndsListener()
        val processors = FileProcessorLocator(settings).load() + ktFilesListener
        val facade = DetektFacade.create(settings, providers, processors)
        val result = facade.run()
        IssueReporter(result, context).run()
        ProjectMeasurementStorage(result, context).run()
        FileProcessor(context, ktFilesListener.kotlinFiles).run()
    }
}
