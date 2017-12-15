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
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.gitlab.arturbosch.detekt.sonar.foundation.ConstantsKt.KOTLIN_FILE_SUFFIX;

@BatchSide
public class KotlinFileSystem {

	private final FileSystem fileSystem;
	private final FilePredicates predicates;
	private final FilePredicate isKotlinLanguage;
	private final FilePredicate isMainTypeFile;

	public KotlinFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
		this.predicates = fileSystem.predicates();
		this.isKotlinLanguage = predicates.hasLanguage(ConstantsKt.KOTLIN_KEY);
		this.isMainTypeFile = predicates.hasType(Type.MAIN);
	}

	public boolean hasGroovyFiles() {
		return fileSystem.hasFiles(isKotlinLanguage);
	}

	public List<File> sourceFiles() {
		Iterable<File> files = fileSystem.files(predicates.and(isKotlinLanguage, isMainTypeFile));
		List<File> list = new ArrayList<>();
		files.iterator().forEachRemaining(list::add);
		return list;
	}

	public List<InputFile> groovyInputFiles() {
		Iterable<InputFile> inputFiles = fileSystem.inputFiles(isKotlinLanguage);
		List<InputFile> list = new ArrayList<>();
		inputFiles.iterator().forEachRemaining(list::add);
		return list;
	}

	public List<InputFile> sourceInputFiles() {
		Iterable<InputFile> inputFiles = fileSystem.inputFiles(predicates.and(isKotlinLanguage, isMainTypeFile));
		List<InputFile> list = new ArrayList<>();
		inputFiles.iterator().forEachRemaining(list::add);
		return list;
	}


	public InputFile findResourceByClassName(String className) {
		FilePredicates predicates = fileSystem.predicates();
		return fileSystem.inputFile(predicates.and(
				predicates.matchesPathPattern("**/" + className.replace('.', '/') + KOTLIN_FILE_SUFFIX),
				predicates.hasLanguage(ConstantsKt.KOTLIN_KEY),
				predicates.hasType(InputFile.Type.MAIN)));
	}

	public InputFile sourceInputFileFromRelativePath(String relativePath) {
		return fileSystem.inputFile(predicates.and(
				predicates.matchesPathPattern("**/" + relativePath), isKotlinLanguage, isMainTypeFile));
	}

	public File baseDir() {
		return fileSystem.baseDir();
	}

}
