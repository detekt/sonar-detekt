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
package io.gitlab.arturbosch.detekt.sonar.jacoco;

import io.gitlab.arturbosch.detekt.sonar.foundation.ConstantsKt;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.config.Settings;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;

public class JaCoCoOverallSensor implements Sensor {

	public static final String JACOCO_OVERALL = "jacoco-overall.exec";

	private final JaCoCoConfiguration configuration;
	private final KotlinFileSystem fileSystem;
	private final PathResolver pathResolver;
	private final Settings settings;

	public JaCoCoOverallSensor(JaCoCoConfiguration configuration, KotlinFileSystem fileSystem, PathResolver pathResolver, Settings settings) {
		this.configuration = configuration;
		this.pathResolver = pathResolver;
		this.fileSystem = fileSystem;
		this.settings = settings;
	}

	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor
				.name("Groovy JaCoCo Overall")
				.onlyOnLanguage(ConstantsKt.KOTLIN_KEY);
	}

	@Override
	public void execute(SensorContext context) {
		File reportUTs = pathResolver.relativeFile(fileSystem.baseDir(), configuration.getReportPath());
		File reportITs = pathResolver.relativeFile(fileSystem.baseDir(), configuration.getItReportPath());
		if (shouldExecuteOnProject()) {
			File reportOverall = new File(context.fileSystem().workDir(), JACOCO_OVERALL);
			reportOverall.getParentFile().mkdirs();
			JaCoCoReportMerger.mergeReports(reportOverall, reportUTs, reportITs);
			new OverallAnalyzer(reportOverall).analyse(context);
		}
	}

	// VisibleForTesting
	boolean shouldExecuteOnProject() {
		File baseDir = fileSystem.baseDir();
		File reportUTs = pathResolver.relativeFile(baseDir, configuration.getReportPath());
		File reportITs = pathResolver.relativeFile(baseDir, configuration.getItReportPath());
		boolean foundOneReport = reportUTs.exists() || reportITs.exists();
		boolean shouldExecute = configuration.shouldExecuteOnProject(foundOneReport);
		if (!foundOneReport && shouldExecute) {
			JaCoCoExtensions.logger().info("JaCoCoOverallSensor: JaCoCo reports not found.");
		}
		return shouldExecute;
	}

	class OverallAnalyzer extends AbstractAnalyzer {
		private final File report;

		OverallAnalyzer(File report) {
			super(fileSystem, pathResolver, settings);
			this.report = report;
		}

		@Override
		protected CoverageType coverageType() {
			return CoverageType.OVERALL;
		}

		@Override
		protected String getReportPath() {
			return report.getAbsolutePath();
		}
	}

}
