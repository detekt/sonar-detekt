package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.processors.COMPLEXITY_KEY
import io.gitlab.arturbosch.detekt.core.processors.NUMBER_OF_COMMENT_LINES_KEY
import io.gitlab.arturbosch.detekt.core.processors.SLOC_KEY
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.jetbrains.kotlin.psi.KtFile
import org.sonar.api.batch.fs.InputComponent
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.measures.CoreMetrics.COMMENT_LINES
import org.sonar.api.measures.CoreMetrics.COMPLEXITY
import org.sonar.api.measures.CoreMetrics.NCLOC
import org.sonar.api.measures.Metric

/**
 * Class responsible for processing individual [KtFile]s and extracting useful metrics.
 */
class FileMeasurementStorage(private val context: SensorContext) {

	fun save(file: KtFile, inputComponent: InputComponent) {
		save(file, inputComponent, SLOC_KEY, NCLOC)
		save(file, inputComponent, NUMBER_OF_COMMENT_LINES_KEY, COMMENT_LINES)
		save(file, inputComponent, COMPLEXITY_KEY, COMPLEXITY)
	}

	private fun save(file: KtFile, inputComponent: InputComponent,
					 dataKey: Key<Int>, metricKey: Metric<Int>) {
		file.getUserData(dataKey)?.let {
			context.newMeasure<Int>()
					.withValue(it)
					.forMetric(metricKey)
					.on(inputComponent)
					.save()
		}
	}

}
