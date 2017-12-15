package io.gitlab.arturbosch.detekt.sonar.sensor

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.gitlab.arturbosch.detekt.sonar.foundation.KOTLIN_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.PATH_FILTERS_KEY
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.DefaultFileSystem
import org.sonar.api.batch.fs.internal.DefaultInputDir
import org.sonar.api.batch.fs.internal.DefaultInputFile
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.config.Settings
import org.sonar.api.measures.CoreMetrics.COMMENT_LINES
import org.sonar.api.measures.CoreMetrics.COMPLEXITY
import org.sonar.api.measures.CoreMetrics.NCLOC
import org.sonar.api.measures.FileLinesContext
import org.sonar.api.measures.FileLinesContextFactory
import java.io.File

class DetektSensorTest {

	private val resourcesDir = File(RESOURCES_PATH)
	private val sourceDir = File(RESOURCES_PATH, KOTLIN_PATH)
	private val settings = Settings().setProperty(PATH_FILTERS_KEY, KOTLIN_PATH)

	private lateinit var context: SensorContextTester
	private lateinit var fileSystem: DefaultFileSystem
	private lateinit var fileLinesContextFactory: FileLinesContextFactory
	private lateinit var sensor: DetektSensor
	private lateinit var fileLinesContext: FileLinesContext

	@Before
	fun setUp() {
		// Recreating all mutable objects to avoid retaining states across tests
		context = SensorContextTester.create(resourcesDir).setSettings(settings)
		fileSystem = context.fileSystem().add(DefaultInputDir("", ""))
		sensor = DetektSensor()

		fileLinesContextFactory = mock()
		fileLinesContext = mock()

		whenever(fileLinesContextFactory.createFor(any<InputFile>())).thenReturn(fileLinesContext)
	}

	@Test
	fun `measures a single kotlin file`() {
		val kotlinFileKey = addMockFile("KotlinFile.kt")

		sensor.execute(context)

		assertThat(context.measure(kotlinFileKey, NCLOC).value()).isEqualTo(2)
		assertThat(context.measure(kotlinFileKey, COMMENT_LINES).value()).isEqualTo(0)
	}

	@Test
	fun `measures two classes`() {
		val a = addMockFile("a/AClassOne.kt")
		val b = addMockFile("b/BClassTwo.kt")

		sensor.execute(context)

		assertThat(context.measure(a, NCLOC).value()).isEqualTo(9)
		assertThat(context.measure(a, COMMENT_LINES).value()).isEqualTo(3)
		assertThat(context.measure(a, COMPLEXITY).value()).isEqualTo(1)
		assertThat(context.measure(b, NCLOC).value()).isEqualTo(6)
		assertThat(context.measure(b, COMMENT_LINES).value()).isEqualTo(3)
		assertThat(context.measure(b, COMPLEXITY).value()).isEqualTo(0)
	}

	private fun addMockFile(filePath: String): String {
		val sourceFile = File(sourceDir, filePath)
		val kotlinFile = DefaultInputFile(RESOURCES_PATH, "$KOTLIN_PATH/$filePath")
				.setLanguage(KOTLIN_KEY)
				.initMetadata(sourceFile.readText())
		fileSystem.add(kotlinFile)
		return kotlinFile.key()
	}

	companion object {
		const val RESOURCES_PATH = "src/test/resources"
		const val KOTLIN_PATH = "kotlin"
	}

}
