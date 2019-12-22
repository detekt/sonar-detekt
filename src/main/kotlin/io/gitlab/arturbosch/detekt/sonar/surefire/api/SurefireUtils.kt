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
package io.gitlab.arturbosch.detekt.sonar.surefire.api

import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.config.Configuration
import org.sonar.api.scan.filesystem.PathResolver
import org.sonar.api.utils.log.Loggers
import java.io.File

/**
 * Adapted from sonar's java plugin and translated to kotlin.
 */
object SurefireUtils {

    private val LOGGER = Loggers.get(SurefireUtils::class.java)

    const val SUREFIRE_REPORTS_PATH_PROPERTY = "sonar.junit.reportsPath"
    const val SUREFIRE_REPORT_PATHS_PROPERTY = "sonar.junit.reportPaths"

    /**
     * Find the directories containing the surefire reports.
     * @param settings Analysis settings.
     * @param fs FileSystem containing indexed files.
     * @param pathResolver Path solver.
     * @return The directories containing the surefire reports or
     * default one (target/surefire-reports) if not found (not configured or not found).
     */
    @Suppress("ReturnCount")
    fun getReportsDirectories(settings: Configuration, fs: FileSystem, pathResolver: PathResolver): List<File> {
        val dir = getReportsDirectoryFromDeprecatedProperty(settings, fs, pathResolver)
        val dirs = getReportsDirectoriesFromProperty(settings, fs, pathResolver)
        if (dirs != null) {
            if (dir != null) {
                // both properties are set, deprecated property ignored
                LOGGER.debug(
                    "Property '{}' is deprecated and will be ignored, as property '{}' is also set.",
                    SUREFIRE_REPORTS_PATH_PROPERTY,
                    SUREFIRE_REPORT_PATHS_PROPERTY
                )
            }
            return dirs
        }
        if (dir != null) {
            LOGGER.info(
                "Property '{}' is deprecated. Use property '{}' instead.",
                SUREFIRE_REPORTS_PATH_PROPERTY,
                SUREFIRE_REPORT_PATHS_PROPERTY
            )
            return listOf(dir)
        }
        // both properties are not set
        return listOf(File(fs.baseDir(), "target/surefire-reports"))
    }

    private fun getReportsDirectoriesFromProperty(
        settings: Configuration,
        fs: FileSystem,
        pathResolver: PathResolver
    ): List<File>? {
        return if (settings.hasKey(SUREFIRE_REPORT_PATHS_PROPERTY)) {
            settings.getStringArray(SUREFIRE_REPORT_PATHS_PROPERTY)
                .asSequence()
                .map { it.trim() }
                .mapNotNull { getFileFromPath(fs, pathResolver, it) }
                .toList()
        } else {
            null
        }
    }

    private fun getReportsDirectoryFromDeprecatedProperty(
        settings: Configuration,
        fs: FileSystem,
        pathResolver: PathResolver
    ): File? {
        if (settings.hasKey(SUREFIRE_REPORTS_PATH_PROPERTY)) {
            val path = settings.get(SUREFIRE_REPORTS_PATH_PROPERTY).orElse(null)
            if (path != null) {
                return getFileFromPath(fs, pathResolver, path)
            }
        }
        return null
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getFileFromPath(fs: FileSystem, pathResolver: PathResolver, path: String): File? {
        try {
            return pathResolver.relativeFile(fs.baseDir(), path)
        } catch (e: Exception) {
            // exceptions on file not found was only occurring with SQ 5.6 LTS, not with SQ 6.4
            LOGGER.info("Surefire report path: {}/{} not found.", fs.baseDir(), path)
        }
        return null
    }
}
