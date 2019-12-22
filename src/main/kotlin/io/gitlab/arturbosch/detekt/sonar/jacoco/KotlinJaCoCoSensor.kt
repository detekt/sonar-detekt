/*
 * SonarQube Java
 * Copyright (C) 2010-2017 SonarSource SA
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
package io.gitlab.arturbosch.detekt.sonar.jacoco

import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.KotlinClasspath
import io.gitlab.arturbosch.detekt.sonar.foundation.unwrap
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.config.Configuration
import org.sonar.plugins.jacoco.JaCoCoExtensions.IT_REPORT_PATH_PROPERTY
import org.sonar.plugins.jacoco.JaCoCoExtensions.LOG
import org.sonar.plugins.jacoco.JaCoCoExtensions.REPORT_MISSING_FORCE_ZERO
import org.sonar.plugins.jacoco.JaCoCoExtensions.REPORT_PATHS_PROPERTY
import org.sonar.plugins.jacoco.JaCoCoExtensions.REPORT_PATH_PROPERTY
import org.sonar.plugins.jacoco.JaCoCoReportMerger
import org.sonar.plugins.java.api.JavaResourceLocator
import java.io.File
import java.util.HashSet

@Suppress("ALL")
open class KotlinJaCoCoSensor(
    fileSystem: FileSystem,
    javaResourceLocator: JavaResourceLocator,
    private val kotlinClasspath: KotlinClasspath
) : Sensor {
    private val javaResourceLocator = KotlinJavaResourceLocator(javaResourceLocator, fileSystem)

    override fun describe(descriptor: SensorDescriptor) {
        descriptor.onlyOnLanguage(KEY).name("KotlinJaCoCoSensor")
    }

    override fun execute(context: SensorContext) {
        if (context.config().hasKey(REPORT_MISSING_FORCE_ZERO)) {
            LOG.warn("Property '{}' is deprecated and its value will be ignored.", REPORT_MISSING_FORCE_ZERO)
        }
        val reportPaths = getReportPaths(context)
        if (reportPaths.isEmpty()) {
            return
        }
        // Merge JaCoCo reports
        val reportMerged: File
        if (reportPaths.size == 1) {
            reportMerged = reportPaths.iterator().next()
        } else {
            reportMerged = File(context.fileSystem().workDir(), JACOCO_MERGED_FILENAME)
            reportMerged.parentFile.mkdirs()
            JaCoCoReportMerger.mergeReports(reportMerged, *reportPaths.toTypedArray())
        }
        KotlinJacocoReportAnalyzer(javaResourceLocator, kotlinClasspath, report = reportMerged).analyse(context)
    }

    override fun toString(): String {
        return javaClass.simpleName
    }

    companion object {

        private const val JACOCO_MERGED_FILENAME = "jacoco-merged.exec"

        private fun getReportPaths(context: SensorContext): Set<File> {
            val reportPaths = HashSet<File>()
            val config = context.config()
            val fs = context.fileSystem()
            for (reportPath in config.getStringArray(REPORT_PATHS_PROPERTY)) {
                val report = fs.resolvePath(reportPath)
                if (!report.isFile) {
                    if (config.hasKey(REPORT_PATHS_PROPERTY)) {
                        LOG.info("JaCoCo report not found: '{}'", reportPath)
                    }
                } else {
                    reportPaths.add(report)
                }
            }
            if (config.hasKey(REPORT_PATH_PROPERTY)) {
                warnUsageOfDeprecatedProperty(config, REPORT_PATH_PROPERTY)
                val report = fs.resolvePath(config.get(REPORT_PATH_PROPERTY).unwrap()!!)
                if (!report.isFile) {
                    LOG.info("JaCoCo UT report not found: '{}'", config.get(REPORT_PATH_PROPERTY).unwrap())
                } else {
                    reportPaths.add(report)
                }
            }
            if (config.hasKey(IT_REPORT_PATH_PROPERTY)) {
                warnUsageOfDeprecatedProperty(config, IT_REPORT_PATH_PROPERTY)
                val report = fs.resolvePath(config.get(IT_REPORT_PATH_PROPERTY).unwrap()!!)
                if (!report.isFile) {
                    LOG.info("JaCoCo IT report not found: '{}'", config.get(IT_REPORT_PATH_PROPERTY).unwrap())
                } else {
                    reportPaths.add(report)
                }
            }
            return reportPaths
        }

        private fun warnUsageOfDeprecatedProperty(config: Configuration, reportPathProperty: String) {
            if (!config.hasKey(REPORT_PATHS_PROPERTY)) {
                LOG.warn("Property '{}' is deprecated. Please use '{}' instead.", reportPathProperty, REPORT_PATHS_PROPERTY)
            }
        }
    }
}
