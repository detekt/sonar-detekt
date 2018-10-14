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
package io.gitlab.arturbosch.detekt.sonar.surefire.data

/**
 * Adapted from sonar's java plugin and translated to kotlin.
 */
@Suppress("unused")
data class UnitTestResult(
		var name: String? = null,
		var testSuiteClassName: String? = null,
		var status: String? = null,
		var stackTrace: String? = null,
		var message: String? = null,
		var durationMilliseconds: Long = 0L
) {
	val isErrorOrFailure: Boolean get() = STATUS_ERROR == status || STATUS_FAILURE == status

	val isError: Boolean get() = STATUS_ERROR == status

	companion object {
		const val STATUS_OK = "ok"
		const val STATUS_ERROR = "error"
		const val STATUS_FAILURE = "failure"
		const val STATUS_SKIPPED = "skipped"
	}
}
