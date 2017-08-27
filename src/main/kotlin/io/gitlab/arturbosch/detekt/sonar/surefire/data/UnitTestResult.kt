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

class UnitTestResult {
    private var name: String? = null
    private var status: String? = null
    private var stackTrace: String? = null
    private var message: String? = null
    private var durationMilliseconds = 0L

    fun getName(): String? {
        return name
    }

    fun setName(name: String): UnitTestResult {
        this.name = name
        return this
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String): UnitTestResult {
        this.status = status
        return this
    }

    fun getStackTrace(): String? {
        return stackTrace
    }

    fun setStackTrace(stackTrace: String): UnitTestResult {
        this.stackTrace = stackTrace
        return this
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String): UnitTestResult {
        this.message = message
        return this
    }

    fun getDurationMilliseconds(): Long {
        return durationMilliseconds
    }

    fun setDurationMilliseconds(l: Long): UnitTestResult {
        this.durationMilliseconds = l
        return this
    }

    val isErrorOrFailure: Boolean
        get() = STATUS_ERROR == status || STATUS_FAILURE == status

    val isError: Boolean
        get() = STATUS_ERROR == status

    companion object {
        val STATUS_OK = "ok"
        val STATUS_ERROR = "error"
        val STATUS_FAILURE = "failure"
        val STATUS_SKIPPED = "skipped"
    }
}
