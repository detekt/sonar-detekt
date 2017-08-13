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
package io.gitlab.arturbosch.detekt.sonar.surefire.api

import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.config.Settings
import org.sonar.api.scan.filesystem.PathResolver
import org.sonar.api.utils.log.Loggers
import java.io.File

object SurefireUtils {

    private val LOGGER = Loggers.get(SurefireUtils::class.java)
    private val SUREFIRE_REPORTS_PATH_PROPERTY = "sonar.junit.reportsPath"
    private val LEGACY_SUREFIRE_REPORTS_PATH_PROPERTY = "sonar.junit.reportPaths"

    fun getReportsDirectory(settings: Settings, fs: FileSystem, pathResolver: PathResolver): File {
        var dir = getReportsDirectoryFromProperty(settings, fs, pathResolver)
        if (dir == null) {
            dir = File(fs.baseDir(), "target/surefire-reports")
        }
        return dir
    }

    private fun getReportsDirectoryFromProperty(settings: Settings, fs: FileSystem, pathResolver: PathResolver): File? {
        val path = settings.getString(SUREFIRE_REPORTS_PATH_PROPERTY) ?: settings.getString(LEGACY_SUREFIRE_REPORTS_PATH_PROPERTY)

        if (path != null) {
            try {
                return pathResolver.relativeFile(fs.baseDir(), path)
            } catch (e: Exception) {
                LOGGER.info("Surefire report path: " + fs.baseDir() + "/" + path + " not found.", e)
            }

        }
        return null
    }

}
