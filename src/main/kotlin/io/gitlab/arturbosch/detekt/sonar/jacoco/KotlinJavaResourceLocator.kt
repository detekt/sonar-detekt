package io.gitlab.arturbosch.detekt.sonar.jacoco

import io.gitlab.arturbosch.detekt.sonar.foundation.FILE_SUFFIX
import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.plugins.java.api.JavaResourceLocator

@Suppress("ALL")
class KotlinJavaResourceLocator(
		delegate: JavaResourceLocator,
		private val fileSystem: FileSystem) : JavaResourceLocator by delegate {

	override fun findResourceByClassName(className: String): InputFile? {
		val filePredicates = fileSystem.predicates()
		return fileSystem.inputFile(filePredicates.and(
				filePredicates.matchesPathPattern("**/" + className.replace('.', '/') + FILE_SUFFIX),
				filePredicates.hasLanguage(KEY),
				filePredicates.hasType(InputFile.Type.MAIN)))
	}
}
