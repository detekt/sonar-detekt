package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.measures.Metric

class MeasurementStorage(
		private val detektion: Detektion,
		private val context: SensorContext) {

	fun save(dataKey: Key<Int>, metricKey: Metric<Int>) {
		detektion.getData(dataKey)?.let {
			context.newMeasure<Int>()
					.withValue(it)
					.forMetric(metricKey)
					.on(context.module())
					.save()
		}
	}
}
