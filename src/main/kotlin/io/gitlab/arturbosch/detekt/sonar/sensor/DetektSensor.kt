package io.gitlab.arturbosch.detekt.sonar.sensor

import io.github.detekt.tooling.api.AnalysisResult
import io.github.detekt.tooling.api.DetektProvider
import io.github.detekt.tooling.api.UnexpectedError
import io.gitlab.arturbosch.detekt.api.UnstableApi
import io.gitlab.arturbosch.detekt.sonar.foundation.DETEKT_SENSOR
import io.gitlab.arturbosch.detekt.sonar.foundation.LANGUAGE_KEY
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor

class DetektSensor : Sensor {

    override fun describe(descriptor: SensorDescriptor) {
        descriptor.name(DETEKT_SENSOR).onlyOnLanguage(LANGUAGE_KEY)
    }

    @OptIn(UnstableApi::class)
    override fun execute(context: SensorContext) {
        val spec = createSpec(context)
        val facade = DetektProvider.load().get(spec)
        val result = facade.run()
        checkErrors(result)
        IssueReporter(checkNotNull(result.container), context).run()
    }

    private fun checkErrors(result: AnalysisResult) {
        when (val error = result.error) {
            is UnexpectedError -> throw error.cause
            null -> return
            else -> throw error
        }
    }
}
