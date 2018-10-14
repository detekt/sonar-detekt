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

import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import io.gitlab.arturbosch.detekt.sonar.surefire.api.SurefireUtils
import org.sonar.api.batch.DependedUpon
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.config.Configuration
import org.sonar.api.scan.filesystem.PathResolver
import org.sonar.api.utils.log.Loggers
import java.io.File

/**
 * Adapted from sonar's java plugin and translated to kotlin.
 */
@DependedUpon("surefire-java")
class KotlinSurefireSensor(
		private val kotlinSurefireParser: KotlinSurefireParser,
		private val configuration: Configuration,
		private val fs: FileSystem,
		private val pathResolver: PathResolver
) : Sensor {

	override fun describe(descriptor: SensorDescriptor) {
		descriptor.onlyOnLanguage(KEY).name("KotlinSurefireSensor")
	}

	override fun execute(context: SensorContext) {
		val dir = SurefireUtils.getReportsDirectories(configuration, fs, pathResolver).distinct()
		collect(context, dir)
	}

	private fun collect(context: SensorContext, reportsDir: List<File>) {
		LOGGER.info("parsing {}", reportsDir)
		kotlinSurefireParser.collect(context, reportsDir,
				configuration.hasKey(SurefireUtils.SUREFIRE_REPORT_PATHS_PROPERTY)
						|| configuration.hasKey(SurefireUtils.SUREFIRE_REPORTS_PATH_PROPERTY))
	}

	override fun toString(): String {
		return javaClass.simpleName
	}

	companion object {
		private val LOGGER = Loggers.get(KotlinSurefireSensor::class.java)
	}
}
