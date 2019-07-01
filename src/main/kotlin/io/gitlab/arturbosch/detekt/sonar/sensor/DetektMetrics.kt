package io.gitlab.arturbosch.detekt.sonar.sensor

import org.sonar.api.measures.CoreMetrics
import org.sonar.api.measures.Metric
import org.sonar.api.measures.Metrics

class DetektMetrics : Metrics {

    override fun getMetrics(): MutableList<Metric<Int>> = mutableListOf(
        projectLocMetric,
        projectSlocMetric,
        projectLlocMetric,
        projectClocMetric,
        projectComplexityMetric
    )
}

val projectLocMetric: Metric<Int> = Metric.Builder("loc",
    "Lines of Code", Metric.ValueType.INT)
    .setDescription("Number of lines of code.")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(CoreMetrics.DOMAIN_GENERAL)
    .create()

val projectSlocMetric: Metric<Int> = Metric.Builder("sloc",
    "Source Lines of Code", Metric.ValueType.INT)
    .setDescription("Number of source lines of code.")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(CoreMetrics.DOMAIN_GENERAL)
    .create()

val projectLlocMetric: Metric<Int> = Metric.Builder("lloc",
    "Logical Lines of Code", Metric.ValueType.INT)
    .setDescription("Number of logical lines of code.")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(CoreMetrics.DOMAIN_GENERAL)
    .create()

val projectClocMetric: Metric<Int> = Metric.Builder("cloc",
    "Comment Lines of Code", Metric.ValueType.INT)
    .setDescription("Number of comment lines of code.")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(CoreMetrics.DOMAIN_GENERAL)
    .create()

val projectComplexityMetric: Metric<Int> = Metric.Builder("project_complexity",
    "Project Cyclomatic Complexity", Metric.ValueType.INT)
    .setDescription("Complexity of the whole project based on McCabe.")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(false)
    .setDomain(CoreMetrics.DOMAIN_GENERAL)
    .create()
