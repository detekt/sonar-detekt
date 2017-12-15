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

import org.apache.commons.lang.StringUtils;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.config.Settings;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractAnalyzer {

	public static final String SONAR_GROOVY_BINARIES = "sonar.kotlin.binaries";
	public static final String SONAR_GROOVY_BINARIES_FALLBACK = "sonar.binaries";

	private final List<File> binaryDirs;
	private final File baseDir;
	private final PathResolver pathResolver;
	private final KotlinFileSystem kotlinFileSystem;
	private Map<String, File> classFilesCache;

	public AbstractAnalyzer(KotlinFileSystem kotlinFileSystem, PathResolver pathResolver, Settings settings) {
		this.kotlinFileSystem = kotlinFileSystem;
		baseDir = kotlinFileSystem.baseDir();
		this.pathResolver = pathResolver;
		this.binaryDirs = getFiles(getBinaryDirectories(settings), baseDir);
	}

	private List<String> getBinaryDirectories(Settings settings) {
		if (settings.hasKey(SONAR_GROOVY_BINARIES)) {
			return Arrays.asList(settings.getStringArray(SONAR_GROOVY_BINARIES));
		}
		return Arrays.asList(settings.getStringArray(SONAR_GROOVY_BINARIES_FALLBACK));
	}

	private static List<File> getFiles(List<String> binaryDirectories, File baseDir) {
		List<File> result = new ArrayList<>();
		for (String directory : binaryDirectories) {
			File f = new File(directory);
			if (!f.isAbsolute()) {
				f = new File(baseDir, directory);
			}
			result.add(f);
		}
		return result;
	}

	private InputFile getInputFile(ISourceFileCoverage coverage) {
//		String className = fullyQualifiedClassName(coverage.getPackageName(), coverage.getName());
//		InputFile fileForClass = kotlinFileSystem.findResourceByClassName(className);
		String relativePath = getFileRelativePath(coverage);
		InputFile fileForClass = kotlinFileSystem.sourceInputFileFromRelativePath(relativePath);
		if (fileForClass == null) {
			JaCoCoExtensions.logger().warn("File not found: " + relativePath);
		}
		return fileForClass;
	}

//	private String fullyQualifiedClassName(String packageName, String simpleClassName) {
//		if ("".equals(packageName)) return "";
//		else return packageName + "/" + StringUtils.substringBeforeLast(simpleClassName, ".");
//	}

	private static String getFileRelativePath(ISourceFileCoverage coverage) {
		return coverage.getPackageName() + "/" + coverage.getName();
	}

	public final void analyse(SensorContext context) {
		if (!atLeastOneBinaryDirectoryExists()) {
			JaCoCoExtensions.logger().warn("Project coverage is set to 0% since there is no directories with classes.");
			return;
		}
		classFilesCache = new HashMap<>();
		for (File classesDir : binaryDirs) {
			populateClassFilesCache(classFilesCache, classesDir, "");
		}

		String path = getReportPath();
		if (path == null) {
			JaCoCoExtensions.logger().warn("No jacoco coverage execution file found.");
			return;
		}
		File jacocoExecutionData = pathResolver.relativeFile(baseDir, path);

		readExecutionData(jacocoExecutionData, context);

		classFilesCache.clear();
	}

	private static void populateClassFilesCache(Map<String, File> classFilesCache, File dir, String path) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				populateClassFilesCache(classFilesCache, file, path + file.getName() + "/");
			} else if (file.getName().endsWith(".class")) {
				String className = path + StringUtils.removeEnd(file.getName(), ".class");
				classFilesCache.put(className, file);
			}
		}
	}

	private boolean atLeastOneBinaryDirectoryExists() {
		if (binaryDirs.isEmpty()) {
			JaCoCoExtensions.logger().warn("No binary directories defined.");
		}
		for (File binaryDir : binaryDirs) {
			JaCoCoExtensions.logger().info("\tChecking binary directory: {}", binaryDir.toString());
			if (binaryDir.exists()) {
				return true;
			}
		}
		return false;
	}

	public final void readExecutionData(File jacocoExecutionData, SensorContext context) {
		ExecutionDataVisitor executionDataVisitor = new ExecutionDataVisitor();

		File fileToAnalyze = jacocoExecutionData;
		if (fileToAnalyze == null || !fileToAnalyze.isFile()) {
			JaCoCoExtensions.logger().warn("Project coverage is set to 0% as no JaCoCo execution data has been dumped: {}", jacocoExecutionData);
			fileToAnalyze = null;
		} else {
			JaCoCoExtensions.logger().info("Analysing {}", fileToAnalyze);
		}
		JaCoCoReportReader jacocoReportReader = new JaCoCoReportReader(fileToAnalyze).readJacocoReport(executionDataVisitor, executionDataVisitor);

		CoverageBuilder coverageBuilder = jacocoReportReader.analyzeFiles(executionDataVisitor.getMerged(), classFilesCache.values());
		int analyzedResources = 0;
		for (ISourceFileCoverage coverage : coverageBuilder.getSourceFiles()) {
			InputFile groovyFile = getInputFile(coverage);
			if (groovyFile != null) {
				NewCoverage newCoverage = context.newCoverage().onFile(groovyFile).ofType(coverageType());
				analyzeFile(newCoverage, groovyFile, coverage);
				newCoverage.save();
				analyzedResources++;
			}
		}
		if (analyzedResources == 0) {
			JaCoCoExtensions.logger().warn("Coverage information was not collected. Perhaps you forget to include debug information into compiled classes?");
		}
	}

	private static void analyzeFile(NewCoverage newCoverage, InputFile groovyFile, ISourceFileCoverage coverage) {
		for (int lineId = coverage.getFirstLine(); lineId <= coverage.getLastLine(); lineId++) {
			int hits = -1;
			ILine line = coverage.getLine(lineId);
			boolean ignore = false;
			switch (line.getInstructionCounter().getStatus()) {
				case ICounter.FULLY_COVERED:
				case ICounter.PARTLY_COVERED:
					hits = 1;
					break;
				case ICounter.NOT_COVERED:
					hits = 0;
					break;
				case ICounter.EMPTY:
					ignore = true;
					break;
				default:
					ignore = true;
					JaCoCoExtensions.logger().warn("Unknown status for line {} in {}", lineId, groovyFile);
					break;
			}
			if (ignore) {
				continue;
			}
			try {
				newCoverage.lineHits(lineId, hits);
			} catch (IllegalStateException stateException) {
				JaCoCoExtensions.logger().warn("cannot set coverage {}", stateException.getMessage());
			}

			ICounter branchCounter = line.getBranchCounter();
			int conditions = branchCounter.getTotalCount();
			if (conditions > 0) {
				int coveredConditions = branchCounter.getCoveredCount();
				newCoverage.conditions(lineId, conditions, coveredConditions);
			}
		}
	}

	protected abstract CoverageType coverageType();

	protected abstract String getReportPath();
}
