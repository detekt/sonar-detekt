package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.processors.commentLinesKey
import io.gitlab.arturbosch.detekt.core.processors.complexityKey
import io.gitlab.arturbosch.detekt.core.processors.sourceLinesKey
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
        save(file, inputComponent, sourceLinesKey, NCLOC)
        save(file, inputComponent, commentLinesKey, COMMENT_LINES)
        save(file, inputComponent, complexityKey, COMPLEXITY)
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
