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

import io.gitlab.arturbosch.detekt.sonar.utils.XmlStreamHandler
import org.codehaus.staxmate.`in`.ElementFilter
import org.codehaus.staxmate.`in`.SMEvent
import org.codehaus.staxmate.`in`.SMHierarchicCursor
import org.codehaus.staxmate.`in`.SMInputCursor
import org.sonar.api.utils.ParsingUtils
import java.text.ParseException
import java.util.*
import javax.xml.stream.XMLStreamException

class SurefireStaxHandler(private val index: UnitTestIndex) : XmlStreamHandler {

    @Throws(XMLStreamException::class)
    override fun stream(rootCursor: SMHierarchicCursor) {
        val testSuite = rootCursor.constructDescendantCursor(ElementFilter("testsuite"))
        var testSuiteEvent: SMEvent?
        testSuiteEvent = testSuite.next
        while (testSuiteEvent != null) {
            if (testSuiteEvent.compareTo(SMEvent.START_ELEMENT) == 0) {
                val testSuiteClassName = testSuite.getAttrValue("name")
                if (testSuiteClassName.contains("$")) {
                    // test suites for inner classes are ignored
                    return
                }
                handleTestCases(testSuiteClassName, testSuite.childCursor(ElementFilter("testcase")))
            }
            testSuiteEvent = testSuite.next
        }
    }

    @Throws(XMLStreamException::class)
    private fun handleTestCases(testSuiteClassName: String, testCase: SMInputCursor) {
        var event: SMEvent?
        event = testCase.next
        while (event != null) {
            if (event.compareTo(SMEvent.START_ELEMENT) == 0) {
                val testClassName = getClassname(testCase, testSuiteClassName)
                val classReport = index.index(testClassName)
                parseTestCase(testCase, classReport)
            }
            event = testCase.next
        }
    }

    @Throws(XMLStreamException::class)
    private fun getClassname(testCaseCursor: SMInputCursor, defaultClassname: String): String {
        var testClassName = testCaseCursor.getAttrValue("classname")
        if (testClassName.isNotBlank() && testClassName.endsWith(")")) {
            testClassName = testClassName.substring(0, testClassName.indexOf('('))
        }
        return if (testClassName.isBlank()) defaultClassname else testClassName
    }

    @Throws(XMLStreamException::class)
    private fun parseTestCase(testCaseCursor: SMInputCursor, report: UnitTestClassReport) {
        report.add(parseTestResult(testCaseCursor))
    }

    @Throws(XMLStreamException::class)
    private fun setStackAndMessage(result: UnitTestResult, stackAndMessageCursor: SMInputCursor) {
        result.setMessage(stackAndMessageCursor.getAttrValue("message"))
        val stack = stackAndMessageCursor.collectDescendantText()
        result.setStackTrace(stack)
    }

    @Throws(XMLStreamException::class)
    private fun parseTestResult(testCaseCursor: SMInputCursor): UnitTestResult {
        val detail = UnitTestResult()
        val name = getTestCaseName(testCaseCursor)
        detail.setName(name)

        var status = UnitTestResult.STATUS_OK
        var duration = getTimeAttributeInMS(testCaseCursor)

        val childNode = testCaseCursor.descendantElementCursor()
        if (childNode.next != null) {
            val elementName = childNode.localName
            when (elementName) {
                "skipped" -> {
                    status = UnitTestResult.STATUS_SKIPPED
                    // bug with surefire reporting wrong time for skipped tests
                    duration = 0L

                }
                "failure" -> {
                    status = UnitTestResult.STATUS_FAILURE
                    setStackAndMessage(detail, childNode)

                }
                "error" -> {
                    status = UnitTestResult.STATUS_ERROR
                    setStackAndMessage(detail, childNode)
                }
            }
        }
        while (childNode.next != null) {
            // make sure we loop till the end of the elements cursor
        }
        detail.setDurationMilliseconds(duration)
        detail.setStatus(status)
        return detail
    }

    @Throws(XMLStreamException::class)
    private fun getTimeAttributeInMS(testCaseCursor: SMInputCursor): Long {
        // hardcoded to Locale.ENGLISH see http://jira.codehaus.org/browse/SONAR-602
        try {
            val time = ParsingUtils.parseNumber(testCaseCursor.getAttrValue("time"), Locale.ENGLISH)
            return if (!java.lang.Double.isNaN(time)) ParsingUtils.scaleValue(time * 1000, 3).toLong() else 0L
        } catch (e: ParseException) {
            throw XMLStreamException(e)
        }

    }

    @Throws(XMLStreamException::class)
    private fun getTestCaseName(testCaseCursor: SMInputCursor): String {
        val classname = testCaseCursor.getAttrValue("classname")
        val name = testCaseCursor.getAttrValue("name")
        return if (classname.contains("$")) {
            classname.substringAfter("$") + "/" + name
        } else {
            name
        }
    }

}
