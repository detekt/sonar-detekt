/*
 * SonarQube Java
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.gitlab.arturbosch.detekt.sonar.surefire

import io.gitlab.arturbosch.detekt.sonar.foundation.FILE_SUFFIX
import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import io.gitlab.arturbosch.detekt.sonar.surefire.data.UnitTestClassReport
import io.gitlab.arturbosch.detekt.sonar.surefire.data.UnitTestIndex
import org.apache.commons.lang.StringUtils
import org.sonar.api.batch.ScannerSide
import org.sonar.api.batch.fs.FilePredicate
import org.sonar.api.batch.fs.FilePredicates
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.component.ResourcePerspectives
import org.sonar.api.measures.CoreMetrics
import org.sonar.api.measures.Metric
import org.sonar.api.test.MutableTestPlan
import org.sonar.api.test.TestCase
import org.sonar.api.utils.log.Loggers
import org.sonar.java.AnalysisException
import java.io.File
import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap
import javax.xml.stream.XMLStreamException
import kotlin.math.max

/**
 * Adapted from sonar's java plugin and translated to kotlin.
 */
@ScannerSide
class KotlinSurefireParser(
    private val perspectives: ResourcePerspectives,
    private val fileSystem: FileSystem
) {

    fun collect(context: SensorContext, reportsDirs: List<File>, reportDirSetByUser: Boolean) {
        val xmlFiles = getReports(reportsDirs, reportDirSetByUser)
        if (xmlFiles.isNotEmpty()) {
            parseFiles(context, xmlFiles)
        }
    }

    private fun parseFiles(context: SensorContext, reports: List<File>) {
        val index = UnitTestIndex()
        parseFiles(reports, index)
        sanitize(index)
        save(index, context)
    }

    private fun save(index: UnitTestIndex, context: SensorContext) {
        var negativeTimeTestNumber: Long = 0
        val indexByInputFile = mapToInputFile(index.indexByClassname)
        for ((key, report) in indexByInputFile) {
            if (report.tests > 0) {
                negativeTimeTestNumber += report.negativeTimeTestNumber
                save(report, key, context)
            }
        }
        if (negativeTimeTestNumber > 0) {
            LOGGER.warn(
                "There is {} test(s) reported with negative time by surefire, total duration may not be accurate.",
                negativeTimeTestNumber
            )
        }
    }

    private fun mapToInputFile(
        indexByClassname: Map<String, UnitTestClassReport>
    ): Map<InputFile, UnitTestClassReport> {
        val result = HashMap<InputFile, UnitTestClassReport>()
        for ((className, index) in indexByClassname) {
            val resource = getUnitTestResource(className, index)
            if (resource != null) {
                val report = (result as MutableMap<InputFile, UnitTestClassReport>)
                    .computeIfAbsent(resource) { UnitTestClassReport() }
                // in case of repeated/parameterized tests (JUnit 5.x) we may end up with tests having the same name
                index.results.forEach { report.add(it) }
            } else {
                LOGGER.debug("Resource not found: {}", className)
            }
        }
        return result
    }

    private fun save(report: UnitTestClassReport, inputFile: InputFile, context: SensorContext) {
        val testsCount = report.tests - report.skipped
        saveMeasure(context, inputFile, CoreMetrics.SKIPPED_TESTS, report.skipped)
        saveMeasure(context, inputFile, CoreMetrics.TESTS, testsCount)
        saveMeasure(context, inputFile, CoreMetrics.TEST_ERRORS, report.errors)
        saveMeasure(context, inputFile, CoreMetrics.TEST_FAILURES, report.failures)
        saveMeasure(context, inputFile, CoreMetrics.TEST_EXECUTION_TIME, report.durationMilliseconds)
        saveResults(inputFile, report)
    }

    private fun saveResults(testFile: InputFile, report: UnitTestClassReport) {
        for (unitTestResult in report.results) {
            val testPlan = perspectives.`as`(MutableTestPlan::class.java, testFile)
            testPlan?.addTestCase(unitTestResult.name)
                ?.setDurationInMs(max(unitTestResult.durationMilliseconds, 0))
                ?.setStatus(TestCase.Status.of(unitTestResult.status))
                ?.setMessage(unitTestResult.message)
                ?.setStackTrace(unitTestResult.stackTrace)
        }
    }

    private fun getUnitTestResource(className: String, unitTestClassReport: UnitTestClassReport): InputFile? {
        return findKotlinTestByClassname(className)
        // fall back on testSuite class name
        // (repeated and parameterized tests from JUnit 5.0 are using test name as classname)
        // Should be fixed with JUnit 5.1, see: https://github.com/junit-team/junit5/issues/1182
            ?: unitTestClassReport.results
                .asSequence()
                .filter { it.testSuiteClassName != null }
                .map { findKotlinTestByClassname(it.testSuiteClassName!!) }
                .firstOrNull { it != null }
    }

    private fun findKotlinTestByClassname(className: String): InputFile? {
        val fileName = StringUtils.replace(className, ".", "/")
        val predicates = fileSystem.predicates()
        val fileNamePredicates = getFileNamePredicateFromSuffixes(
            predicates,
            fileName,
            arrayOf(FILE_SUFFIX)
        )
        val searchPredicate = predicates.and(predicates.and(
            predicates.hasLanguage(KEY),
            predicates.hasType(InputFile.Type.TEST)
        ), fileNamePredicates)
        return if (fileSystem.hasFiles(searchPredicate)) {
            fileSystem.inputFiles(searchPredicate).iterator().next()
        } else {
            null
        }
    }

    private fun getFileNamePredicateFromSuffixes(
        p: FilePredicates,
        fileName: String,
        suffixes: Array<String>
    ): FilePredicate {
        val fileNamePredicates = ArrayList<FilePredicate>(suffixes.size)
        for (suffix in suffixes) {
            fileNamePredicates.add(p.matchesPathPattern("**/$fileName$suffix"))
        }
        return p.or(fileNamePredicates)
    }

    companion object {

        private val LOGGER = Loggers.get(KotlinSurefireParser::class.java)

        private fun getReports(dirs: List<File>, reportDirSetByUser: Boolean): List<File> =
            dirs.asSequence()
                .map { getReports(it, reportDirSetByUser) }
                .flatten()
                .toList()

        private fun getReports(dir: File, reportDirSetByUser: Boolean): List<File> {
            if (!dir.isDirectory) {
                if (reportDirSetByUser) {
                    LOGGER.error("Reports path not found or is not a directory: " + dir.absolutePath)
                }
                return emptyList()
            }
            var unitTestResultFiles = findXMLFilesStartingWith(dir, "TEST-")
            if (unitTestResultFiles.isEmpty()) {
                // maybe there's only a test suite result file
                unitTestResultFiles = findXMLFilesStartingWith(dir, "TESTS-")
            }
            if (unitTestResultFiles.isEmpty()) {
                LOGGER.warn("Reports path contains no files matching TEST-.*.xml : " + dir.absolutePath)
            }
            return unitTestResultFiles.toList()
        }

        private fun findXMLFilesStartingWith(dir: File, fileNameStart: String): Array<File> =
            dir.listFiles { _, name -> name.startsWith(fileNameStart) && name.endsWith(".xml") }
                ?: emptyArray()

        private fun parseFiles(reports: List<File>, index: UnitTestIndex) {
            val parser = StaxParser(index)
            for (report in reports) {
                try {
                    parser.parse(report)
                } catch (e: XMLStreamException) {
                    throw AnalysisException("Fail to parse the Surefire report: $report", e)
                }

            }
        }

        private fun sanitize(index: UnitTestIndex) {
            for (classname in index.classnames) {
                if (StringUtils.contains(classname, "$")) {
                    // Surefire reports classes whereas sonar supports files
                    val parentClassName = StringUtils.substringBefore(classname, "$")
                    index.merge(classname, parentClassName)
                }
            }
        }

        private fun <T : Serializable> saveMeasure(
            context: SensorContext,
            inputFile: InputFile,
            metric: Metric<T>,
            value: T
        ) {
            context.newMeasure<T>().forMetric(metric).on(inputFile).withValue(value).save()
        }
    }
}
