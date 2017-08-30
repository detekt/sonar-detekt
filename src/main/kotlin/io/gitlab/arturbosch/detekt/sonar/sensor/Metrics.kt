package io.gitlab.arturbosch.detekt.sonar.sensor

import org.sonar.api.measures.CoreMetrics
import org.sonar.api.measures.Metric
import org.sonar.api.measures.Metrics

/**
 * @author Artur Bosch
 */
class DetektMetrics : Metrics {

	override fun getMetrics(): MutableList<Metric<Int>> = mutableListOf(
			LOC_PROJECT,
			SLOC_PROJECT,
			LLOC_PROJECT,
			CLOC_PROJECT,
			MCCABE_PROJECT
	)
}

val LOC_PROJECT: Metric<Int> = Metric.Builder("loc",
		"Lines of Code", Metric.ValueType.INT)
		.setDescription("Number of lines of code.")
		.setDirection(Metric.DIRECTION_NONE)
		.setQualitative(false)
		.setDomain(CoreMetrics.DOMAIN_GENERAL)
		.create<Int>()

val SLOC_PROJECT: Metric<Int> = Metric.Builder("sloc",
		"Source Lines of Code", Metric.ValueType.INT)
		.setDescription("Number of source lines of code.")
		.setDirection(Metric.DIRECTION_NONE)
		.setQualitative(false)
		.setDomain(CoreMetrics.DOMAIN_GENERAL)
		.create<Int>()

val LLOC_PROJECT: Metric<Int> = Metric.Builder("lloc",
		"Logical Lines of Code", Metric.ValueType.INT)
		.setDescription("Number of logical lines of code.")
		.setDirection(Metric.DIRECTION_NONE)
		.setQualitative(false)
		.setDomain(CoreMetrics.DOMAIN_GENERAL)
		.create<Int>()

val CLOC_PROJECT: Metric<Int> = Metric.Builder("cloc",
		"Comment Lines of Code", Metric.ValueType.INT)
		.setDescription("Number of comment lines of code.")
		.setDirection(Metric.DIRECTION_NONE)
		.setQualitative(false)
		.setDomain(CoreMetrics.DOMAIN_GENERAL)
		.create<Int>()

val MCCABE_PROJECT: Metric<Int> = Metric.Builder("project_complexity",
		"Project Cyclomatic Complexity", Metric.ValueType.INT)
		.setDescription("Complexity of the whole project based on McCabe.")
		.setDirection(Metric.DIRECTION_NONE)
		.setQualitative(false)
		.setDomain(CoreMetrics.DOMAIN_GENERAL)
		.create<Int>()
