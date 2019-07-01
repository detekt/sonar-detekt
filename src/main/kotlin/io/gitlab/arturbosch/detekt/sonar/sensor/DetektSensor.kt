package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.DetektFacade
import io.gitlab.arturbosch.detekt.core.KtTreeCompiler
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
        val detektor = DetektFacade.create(settings)
        val compiler = KtTreeCompiler.instance(settings)

        settings.inputPaths.forEach {
            val ktFiles = compiler.compile(it)
            val detektion = detektor.run(it, ktFiles)

            IssueReporter(detektion, context).run()
            ProjectMeasurementStorage(detektion, context).run()
            FileProcessor(context, ktFiles).run()
        }
    }
}
