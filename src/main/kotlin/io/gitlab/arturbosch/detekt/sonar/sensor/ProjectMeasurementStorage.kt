package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.core.processors.COMPLEXITY_KEY
import io.gitlab.arturbosch.detekt.core.processors.LLOC_KEY
import io.gitlab.arturbosch.detekt.core.processors.LOC_KEY
import io.gitlab.arturbosch.detekt.core.processors.NUMBER_OF_COMMENT_LINES_KEY
import io.gitlab.arturbosch.detekt.core.processors.SLOC_KEY
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.measures.Metric

class ProjectMeasurementStorage(private val detektion: Detektion,
								private val context: SensorContext) {

	fun run() {
		save(LOC_KEY, LOC_PROJECT)
		save(SLOC_KEY, SLOC_PROJECT)
		save(LLOC_KEY, LLOC_PROJECT)
		save(NUMBER_OF_COMMENT_LINES_KEY, CLOC_PROJECT)
		save(COMPLEXITY_KEY, MCCABE_PROJECT)
	}

	private fun save(dataKey: Key<Int>, metricKey: Metric<Int>) {
		detektion.getData(dataKey)?.let {
			context.newMeasure<Int>()
					.withValue(it)
					.forMetric(metricKey)
					.on(context.module())
					.save()
		}
	}
}
