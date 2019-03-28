package io.gitlab.arturbosch.detekt.sonar.jacoco

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.sonar.api.batch.fs.FilePredicate
import org.sonar.api.batch.fs.FilePredicates
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.plugins.java.api.JavaResourceLocator
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

const val className = "foo.Bar"

class KotlinJavaResourceLocatorTest : Spek({
    val javaLocator = mockk<JavaResourceLocator>()
    val fileSystem = mockk<FileSystem>()
    val predicates = mockk<FilePredicates>()
    val predicate = mockk<FilePredicate>()
    val kotlinFile = mockk<InputFile>(name = "Bar.kt")
    val javaFile = mockk<InputFile>(name = "Bar.java")

    every { predicates.matchesPathPattern(any()) } returns predicate
    every { predicates.and(any(), any(), any()) } returns predicate
    every { predicates.hasLanguage(any()) } returns predicate
    every { predicates.hasType(any()) } returns predicate

    describe("a locator") {

        val locator = KotlinJavaResourceLocator(javaLocator, fileSystem)

        beforeEach {
            clearMocks(fileSystem)
            every { fileSystem.predicates() } returns predicates
        }

        context("kotlin file present") {

            it("should find kotlin resource") {
                every { fileSystem.inputFile(any()) } returns kotlinFile
                assertThat(locator.findResourceByClassName(className)).isEqualTo(kotlinFile)
            }
        }

        context("no kotlin file present") {

            it("should find java resource") {
                every { fileSystem.inputFile(any()) } returns null
                every { javaLocator.findResourceByClassName(className) } returns javaFile
                assertThat(locator.findResourceByClassName(className)).isEqualTo(javaFile)
            }

            it("should return null if no java resource") {
                every { fileSystem.inputFile(any()) } returns null
                every { javaLocator.findResourceByClassName(className) } returns null
                assertThat(locator.findResourceByClassName(className)).isNull()
            }
        }
    }
})
