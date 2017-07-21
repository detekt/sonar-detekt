package io.gitlab.arturbosch.detekt.sonar.jacoco

import org.sonar.plugins.jacoco.JacocoConfiguration

object JaCoCoExtensions {

	private val sensors = listOf(
			KotlinJaCoCoSensor::class.java,
			KotlinJaCoCoOverallSensor::class.java,
			KotlinJaCoCoItSensor::class.java
	)

	val extensions: List<Any> = listOf(sensors, JacocoConfiguration::class.java)
}