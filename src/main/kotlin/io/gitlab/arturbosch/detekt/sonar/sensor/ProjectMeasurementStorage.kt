package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.core.processors.commentLinesKey
import io.gitlab.arturbosch.detekt.core.processors.complexityKey
import io.gitlab.arturbosch.detekt.core.processors.linesKey
import io.gitlab.arturbosch.detekt.core.processors.logicalLinesKey
import io.gitlab.arturbosch.detekt.core.processors.sourceLinesKey
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.measures.Metric

class ProjectMeasurementStorage(
    private val result: Detektion,
    private val context: SensorContext
) {

    fun run() {
        save(linesKey, projectLocMetric)
        save(sourceLinesKey, projectSlocMetric)
        save(logicalLinesKey, projectLlocMetric)
        save(commentLinesKey, projectClocMetric)
        save(complexityKey, projectComplexityMetric)
    }

    private fun save(dataKey: Key<Int>, metricKey: Metric<Int>) {
        val data = result.getData(dataKey)
        if (data != null) {
            context.newMeasure<Int>()
                .withValue(data)
                .forMetric(metricKey)
                .on(context.module())
                .save()
        }
    }
}
