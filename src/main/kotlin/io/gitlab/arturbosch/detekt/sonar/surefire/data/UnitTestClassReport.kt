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

data class UnitTestClassReport(
        var errors: Int = 0,
        var failures: Int = 0,
        var skipped: Int = 0,
        var tests: Int = 0,
        var durationMilliseconds: Long = 0L,
        var negativeTimeTestNumber: Long = 0L,
        val results: MutableList<UnitTestResult> = mutableListOf()
) {


    fun add(other: UnitTestClassReport): UnitTestClassReport {
        for (otherResult in other.results) {
            add(otherResult)
        }
        return this
    }

    fun add(result: UnitTestResult): UnitTestClassReport {
        results.add(result)
        when {
            result.status == UnitTestResult.STATUS_SKIPPED -> skipped += 1
            result.status == UnitTestResult.STATUS_FAILURE -> failures += 1
            result.status == UnitTestResult.STATUS_ERROR -> errors += 1
        }
        tests += 1
        if (result.durationMilliseconds < 0) {
            negativeTimeTestNumber += 1
        } else {
            durationMilliseconds += result.durationMilliseconds
        }
        return this
    }

}