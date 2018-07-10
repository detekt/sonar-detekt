package io.gitlab.arturbosch.detekt.sonar.jacoco

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.sonar.api.batch.fs.FilePredicates
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.plugins.java.api.JavaResourceLocator

val className = "foo.Bar"

class KotlinJavaResourceLocatorTest : Spek({
    val javaLocator: JavaResourceLocator = mock()
    val fileSystem: FileSystem = mock()
    val predicates: FilePredicates = mock()
    val kotlinFile: InputFile = mock(name = "Bar.kt")
    val javaFile: InputFile = mock(name = "Bar.java")
    whenever(fileSystem.predicates()).thenReturn(predicates)

    describe("a locator") {

        val locator = KotlinJavaResourceLocator(javaLocator, fileSystem)

        on("kotlin file present") {
            whenever(fileSystem.inputFile(anyOrNull())).thenReturn(kotlinFile)

            it("should find kotlin resource") {
                assertThat(locator.findResourceByClassName(className)).isEqualTo(kotlinFile)
            }
        }

        on("no kotlin file present") {
            whenever(fileSystem.inputFile(anyOrNull())).thenReturn(null)

            it("should find java resource") {
                whenever(javaLocator.findResourceByClassName(className)).thenReturn(javaFile)
                assertThat(locator.findResourceByClassName(className)).isEqualTo(javaFile)
            }

            it ("should return null if no java resource") {
                whenever(javaLocator.findResourceByClassName(className)).thenReturn(null)
                assertThat(locator.findResourceByClassName(className)).isNull()
            }
        }
    }
})
