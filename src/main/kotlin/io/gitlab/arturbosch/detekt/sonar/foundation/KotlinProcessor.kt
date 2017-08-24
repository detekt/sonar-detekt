package io.gitlab.arturbosch.detekt.sonar.foundation

import io.gitlab.arturbosch.detekt.api.FileProcessListener
import io.gitlab.arturbosch.detekt.core.FileProcessorLocator
import io.gitlab.arturbosch.detekt.core.processors.COMPLEXITY_KEY
import io.gitlab.arturbosch.detekt.core.processors.LLOC_KEY
import io.gitlab.arturbosch.detekt.core.processors.NUMBER_OF_COMMENT_LINES_KEY
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
class KotlinProcessor(
		private val context: SensorContext,
		fileProcessorLocator: FileProcessorLocator
) {

	private val processors: List<FileProcessListener> = fileProcessorLocator.load()

	fun process(file: KtFile, inputComponent: InputComponent) {
		processors.forEach { it.onProcess(file) }
		saveResults(file, inputComponent)
	}

	private fun saveResults(file: KtFile, inputComponent: InputComponent) {
		save(file, inputComponent, LLOC_KEY, NCLOC)
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
