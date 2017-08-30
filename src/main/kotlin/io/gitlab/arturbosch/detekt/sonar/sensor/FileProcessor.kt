package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.KtCompiler
import io.gitlab.arturbosch.detekt.sonar.foundation.KotlinSyntax
import org.jetbrains.kotlin.psi.KtFile
import org.sonar.api.batch.sensor.SensorContext

/**
 * @author Artur Bosch
 */
class FileProcessor(private val context: SensorContext,
					private val ktFiles: List<KtFile>) {

	private val fileStorage = FileMeasurementStorage(context)
	private val fileSystem = context.fileSystem()

	fun run() {
		for (ktFile in ktFiles) {
			fileSystem.inputFile { it.relativePath() == ktFile.getUserData(KtCompiler.RELATIVE_PATH) }?.let {
				fileStorage.save(ktFile, it)
				KotlinSyntax.processFile(it, ktFile, context)
			}
		}
	}
}
