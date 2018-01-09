package io.gitlab.arturbosch.detekt.sonar.jacoco

import io.gitlab.arturbosch.detekt.sonar.foundation.FILE_SUFFIX
import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.plugins.java.api.JavaResourceLocator

/**
 * Locate kotlin or java input file for given class name.
 */
@Suppress("ALL")
class KotlinJavaResourceLocator(
        private val javaResourceLocator: JavaResourceLocator,
        private val fileSystem: FileSystem) : JavaResourceLocator by javaResourceLocator {

	override fun findResourceByClassName(className: String): InputFile? {
        return kotlinResource(className) ?: javaResourceLocator.findResourceByClassName(className)
	}

	private fun kotlinResource(className: String): InputFile? {
        val filePredicates = fileSystem.predicates()
        return fileSystem.inputFile(filePredicates.and(
                filePredicates.matchesPathPattern("**/" + className.replace('.', '/') + FILE_SUFFIX),
                filePredicates.hasLanguage(KEY),
                filePredicates.hasType(InputFile.Type.MAIN)))
	}
}
