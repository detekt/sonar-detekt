package io.gitlab.arturbosch.detekt.sonar.sensor

import org.sonar.api.measures.CoreMetrics
import org.sonar.api.measures.Metric
import org.sonar.api.measures.Metrics

/**
 * @author Artur Bosch
 */
class DetektMetrics : Metrics {

	override fun getMetrics(): MutableList<Metric<Int>> = mutableListOf(
			LLOC_PROJECT,
			MCCABE_PROJECT
	)
}

val LLOC_PROJECT: Metric<Int> = Metric.Builder("lloc", "Logical Lines of Code", Metric.ValueType.INT)
		.setDescription("Number of logical lines of code.")
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
