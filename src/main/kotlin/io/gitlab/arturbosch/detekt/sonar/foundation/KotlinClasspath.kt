package io.gitlab.arturbosch.detekt.sonar.foundation

import org.sonar.api.batch.fs.FilePredicates
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.config.Configuration
import org.sonar.api.utils.log.Loggers
import org.sonar.api.utils.log.Profiler
import org.sonar.java.AbstractJavaClasspath
import org.sonar.java.AnalysisException
import org.sonar.java.JavaClasspath

private val type = InputFile.Type.MAIN

/**
 * This class is based on [JavaClasspath] adding kotlin compiled .class files
 * to binaries using the default kotlin .class files location.
 */
class KotlinClasspath(settings: Configuration, fs: FileSystem) : AbstractJavaClasspath(settings, fs, type) {
    private val log = Loggers.get(JavaClasspath::class.java)

    override fun init() {
        if (!initialized) {
            validateLibraries = fs.hasFiles(this.fs.predicates().all())
            val profiler = Profiler.create(log).startInfo("KotlinClasspath initialization")
            initialized = true

            val libraries = this.getFilesFromProperty("sonar.java.libraries")
            val binaryFiles = ArrayList(this.getFilesFromProperty("sonar.java.binaries"))

            // assume a path with `kotlin-classes` within libraries is a kotlin .class files location
            libraries.filter { it.path.contains("kotlin-classes") }.forEach {
                libraries.remove(it)
                binaryFiles.add(it)
            }

            if (binaryFiles.isEmpty() && hasMoreThanOneKotlinFile()) {
                throw AnalysisException(
                    "Please provide compiled classes of your project with sonar.java.binaries property"
                )
            }

            binaries = binaryFiles
            if (libraries.isEmpty() && hasKotlinSources()) {
                log.warn(
                    "Bytecode of dependencies was not provided for analysis of source files," +
                        " you might end up with less precise results." +
                        " Bytecode can be provided using sonar.java.libraries property"
                )
            }

            elements = ArrayList(binaries)
            elements.addAll(libraries)
            profiler.stopInfo()
        }
    }

    private fun hasKotlinSources(): Boolean {
        return fs.hasFiles(ktPredicate(fs.predicates()))
    }

    private fun hasMoreThanOneKotlinFile(): Boolean {
        return fs.inputFiles(ktPredicate(fs.predicates()))
            .filterIndexed { i, _ -> i > 1 }
            .any()
    }

    private fun ktPredicate(p: FilePredicates) = p.and(p.hasLanguage(KEY), p.hasType(type))
}
