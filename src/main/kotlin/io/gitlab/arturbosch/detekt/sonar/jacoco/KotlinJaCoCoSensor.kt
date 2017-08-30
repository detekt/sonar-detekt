package io.gitlab.arturbosch.detekt.sonar.jacoco

import io.gitlab.arturbosch.detekt.sonar.foundation.KOTLIN_KEY
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.component.ResourcePerspectives
import org.sonar.api.scan.filesystem.PathResolver
import org.sonar.java.JavaClasspath
import org.sonar.plugins.jacoco.JaCoCoSensor
import org.sonar.plugins.jacoco.JacocoConfiguration
import org.sonar.plugins.java.api.JavaResourceLocator

class KotlinJaCoCoSensor(
		configuration: JacocoConfiguration,
		perspectives: ResourcePerspectives,
		fileSystem: FileSystem,
		pathResolver: PathResolver,
		javaResourceLocator: JavaResourceLocator,
		javaClasspath: JavaClasspath
) : JaCoCoSensor(
		configuration,
		perspectives,
		fileSystem,
		pathResolver,
		KotlinJavaResourceLocator(javaResourceLocator, fileSystem),
		javaClasspath) {

	override fun describe(descriptor: SensorDescriptor) {
		descriptor.onlyOnLanguage(KOTLIN_KEY).name(javaClass.simpleName)
	}
}
