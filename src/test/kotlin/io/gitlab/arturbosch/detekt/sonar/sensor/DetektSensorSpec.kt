package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.sonar.foundation.LANGUAGE_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.sonar.api.batch.fs.internal.DefaultFileSystem
import org.sonar.api.batch.fs.internal.DefaultInputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.measures.FileLinesContext
import org.sonar.api.measures.FileLinesContextFactory
import org.spekframework.spek2.Spek
import java.io.File

class DetektSensorSpec : Spek({

    val resourcesDir = File(RESOURCES_PATH)
    val sourceDir = File(RESOURCES_PATH, KOTLIN_PATH)
    val settings = MapSettings().setProperty(PATH_FILTERS_KEY, KOTLIN_PATH)

    lateinit var context: SensorContextTester
    lateinit var fileSystem: DefaultFileSystem
    lateinit var fileLinesContextFactory: FileLinesContextFactory
    lateinit var sensor: DetektSensor
    lateinit var fileLinesContext: FileLinesContext

    beforeEachTest {
        // Recreating all mutable objects to avoid retaining states across tests
        context = SensorContextTester.create(resourcesDir.absoluteFile).setSettings(settings)
        fileSystem = context.fileSystem()
        sensor = DetektSensor()

        fileLinesContextFactory = mockk()
        fileLinesContext = mockk()

        every { fileLinesContextFactory.createFor(any()) } returns fileLinesContext
    }

    fun addMockFile(filePath: String): DefaultInputFile {
        val sourceFile = File(sourceDir, filePath)
        val kotlinFile = TestInputFileBuilder(RESOURCES_PATH, "$KOTLIN_PATH/$filePath")
            .setLanguage(LANGUAGE_KEY)
            .initMetadata(sourceFile.readText())
            .build()
        fileSystem.add(kotlinFile)
        return kotlinFile
    }

    test("executes detekt") {
        val file = addMockFile("KotlinFile.kt")

        sensor.execute(context)
        val issues = context.allIssues().filter { it.primaryLocation().inputComponent() == file }

        assertThat(issues).hasSize(7)
    }
}) {

    companion object {
        const val RESOURCES_PATH = "src/test/resources"
        const val KOTLIN_PATH = "kotlin"
    }
}
