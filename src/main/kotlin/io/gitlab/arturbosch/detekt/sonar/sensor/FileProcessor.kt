package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.core.relativePath
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
		val base = context.fileSystem().baseDir().toPath()
		for (ktFile in ktFiles) {
			fileSystem.inputFile {
				base.fileName.resolve(it.relativePath()).toString() == ktFile.relativePath()
			}?.let {
				fileStorage.save(ktFile, it)
				KotlinSyntax.processFile(it, ktFile, context)
			}
		}
	}
}
