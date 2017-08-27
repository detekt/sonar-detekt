/*
 * Sonar Groovy Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import io.gitlab.arturbosch.detekt.sonar.foundation.KOTLIN_FILE_SUFFIX
import io.gitlab.arturbosch.detekt.sonar.foundation.KOTLIN_KEY
import io.gitlab.arturbosch.detekt.sonar.surefire.data.SurefireStaxHandler
import io.gitlab.arturbosch.detekt.sonar.surefire.data.UnitTestClassReport
import io.gitlab.arturbosch.detekt.sonar.surefire.data.UnitTestIndex
import io.gitlab.arturbosch.detekt.sonar.utils.StaxParser
import org.sonar.api.batch.BatchSide
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
import org.sonar.api.utils.MessageException
import org.sonar.api.utils.ParsingUtils
import org.sonar.api.utils.log.Loggers
import java.io.File
import java.io.Serializable
import javax.xml.stream.XMLStreamException

@BatchSide
class KotlinSurefireParser(private val perspectives: ResourcePerspectives, private val fs: FileSystem) {

    fun collect(context: SensorContext, reportsDir: File) {
        val xmlFiles = getReports(reportsDir)
        if (xmlFiles.isNotEmpty()) {
            parseFiles(context, xmlFiles)
        }
    }

    private fun parseFiles(context: SensorContext, reports: Array<File>) {
        val index = UnitTestIndex()
        parseFiles(reports, index)
        sanitize(index)
        save(index, context)
    }

    private fun save(index: UnitTestIndex, context: SensorContext) {
        var negativeTimeTestNumber: Long = 0
        for ((key, report) in index.getIndexByClassname()) {
            if (report.tests > 0) {
                negativeTimeTestNumber += report.negativeTimeTestNumber
                val inputFile = getUnitTestInputFile(key)
                if (inputFile != null) {
                    save(report, inputFile, context)
                } else {
                    LOGGER.warn("Resource not found: {}", key)
                }
            }
        }
        if (negativeTimeTestNumber > 0) {
            LOGGER.warn(
                    "There is {} test(s) reported with negative time by surefire, total duration may not be accurate.",
                    negativeTimeTestNumber
            )
        }
    }

    private fun save(report: UnitTestClassReport, inputFile: InputFile, context: SensorContext) {
        val testsCount = report.tests - report.skipped
        saveMeasure(context, inputFile, CoreMetrics.SKIPPED_TESTS, report.skipped)
        saveMeasure(context, inputFile, CoreMetrics.TESTS, testsCount)
        saveMeasure(context, inputFile, CoreMetrics.TEST_ERRORS, report.errors)
        saveMeasure(context, inputFile, CoreMetrics.TEST_FAILURES, report.failures)
        saveMeasure(context, inputFile, CoreMetrics.TEST_EXECUTION_TIME, report.durationMilliseconds)
        val passedTests = testsCount - report.errors - report.failures
        if (testsCount > 0) {
            val percentage = passedTests * 100.0 / testsCount
            saveMeasure(context, inputFile, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage))
        }
        saveResults(inputFile, report)
    }

    private fun saveResults(testFile: InputFile, report: UnitTestClassReport) {
        for (unitTestResult in report.results) {
            val testPlan = perspectives.`as`(MutableTestPlan::class.java, testFile)
            testPlan?.addTestCase(unitTestResult.getName())?.setDurationInMs(Math.max(unitTestResult.getDurationMilliseconds(), 0))?.setStatus(TestCase.Status.of(unitTestResult.getStatus()))?.setMessage(unitTestResult.getMessage())?.setType(TestCase.TYPE_UNIT)?.setStackTrace(unitTestResult.getStackTrace())
        }
    }

    private fun getUnitTestInputFile(classKey: String): InputFile? {
        val fileName = classKey.replace('.', '/')
        val p = fs.predicates()
        val fileNamePredicates = getFileNamePredicateFromSuffixes(p, fileName, arrayOf(KOTLIN_FILE_SUFFIX))
        val searchPredicate = p.and(p.and(p.hasLanguage(KOTLIN_KEY), p.hasType(InputFile.Type.TEST)), fileNamePredicates)
        return if (fs.hasFiles(searchPredicate)) {
            fs.inputFiles(searchPredicate).iterator().next()
        } else {
            null
        }
    }

    companion object {

        private val LOGGER = Loggers.get(KotlinSurefireParser::class.java)

        private fun getReports(dir: File?): Array<File> {
            if (dir == null) {
                return emptyArray()
            } else if (!dir.isDirectory) {
                LOGGER.warn("Reports path not found: " + dir.absolutePath)
                return emptyArray()
            }
            var unitTestResultFiles = findXMLFilesStartingWith(dir, "TEST-")
            if (unitTestResultFiles.isEmpty()) {
                // maybe there's only a test suite result file
                unitTestResultFiles = findXMLFilesStartingWith(dir, "TESTS-")
            }
            return unitTestResultFiles
        }

        private fun findXMLFilesStartingWith(dir: File, fileNameStart: String): Array<File> {
            return dir.listFiles { _, name -> name.startsWith(fileNameStart) && name.endsWith(".xml") }
        }

        private fun parseFiles(reports: Array<File>, index: UnitTestIndex) {
            val parser = StaxParser(SurefireStaxHandler(index))
            for (report in reports) {
                try {
                    parser.parse(report)
                } catch (e: XMLStreamException) {
                    throw MessageException.of("Fail to parse the Surefire report: " + report, e)
                }

            }
        }

        private fun sanitize(index: UnitTestIndex) {
            for (classname in index.classnames) {
                if (classname.contains("$")) {
                    // Surefire reports classes whereas sonar supports files
                    val parentClassName = classname.substringBefore("$")
                    index.merge(classname, parentClassName)
                }
            }
        }

        private fun getFileNamePredicateFromSuffixes(p: FilePredicates, fileName: String, suffixes: Array<String>): FilePredicate {
            val fileNamePredicates = ArrayList<FilePredicate>(suffixes.size)
            suffixes.mapTo(fileNamePredicates) { p.matchesPathPattern("**/" + fileName + it) }
            return p.or(fileNamePredicates)
        }

        private fun <T : Serializable> saveMeasure(context: SensorContext, inputFile: InputFile, metric: Metric<T>, value: T) {
            context.newMeasure<T>().forMetric(metric).on(inputFile).withValue(value).save()
        }
    }

}
