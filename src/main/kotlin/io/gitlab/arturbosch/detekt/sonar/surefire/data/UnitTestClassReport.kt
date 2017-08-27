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
package io.gitlab.arturbosch.detekt.sonar.surefire.data

class UnitTestClassReport {
    var errors = 0
        private set
    var failures = 0
        private set
    var skipped = 0
        private set
    var tests = 0
        private set
    var durationMilliseconds = 0L
        private set
    var negativeTimeTestNumber = 0L
        private set

    val results: MutableList<UnitTestResult> = ArrayList()

    fun add(other: UnitTestClassReport): UnitTestClassReport {
        for (otherResult in other.results) {
            add(otherResult)
        }
        return this
    }

    fun add(result: UnitTestResult): UnitTestClassReport {
        results.add(result)
        when {
            result.getStatus() == UnitTestResult.STATUS_SKIPPED -> skipped += 1
            result.getStatus() == UnitTestResult.STATUS_FAILURE -> failures += 1
            result.getStatus() == UnitTestResult.STATUS_ERROR -> errors += 1
        }
        tests += 1
        if (result.getDurationMilliseconds() < 0) {
            negativeTimeTestNumber += 1
        } else {
            durationMilliseconds += result.getDurationMilliseconds()
        }
        return this
    }
}
