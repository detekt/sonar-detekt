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
package io.gitlab.arturbosch.detekt.sonar.utils

import com.ctc.wstx.stax.WstxInputFactory
import org.codehaus.staxmate.SMInputFactory
import org.codehaus.staxmate.`in`.SMHierarchicCursor
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

@FunctionalInterface
interface XmlStreamHandler {
    @Throws(XMLStreamException::class)
    fun stream(rootCursor: SMHierarchicCursor)
}

class StaxParser(private val streamHandler: XmlStreamHandler) {
    private val inf: SMInputFactory

    init {
        val xmlFactory = XMLInputFactory.newInstance()
        if (xmlFactory is WstxInputFactory) {
            xmlFactory.configureForLowMemUsage()
            xmlFactory.config.setUndeclaredEntityResolver { _: String, _: String, _: String, namespace: String -> namespace }
        }
        xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, false)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
        xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
        inf = SMInputFactory(xmlFactory)
    }

    fun parse(xmlFile: File) {
        try {
            FileInputStream(xmlFile).use { input -> parse(inf.rootElementCursor(input)) }
        } catch (e: IOException) {
            throw XMLStreamException(e)
        }

    }

    private fun parse(rootCursor: SMHierarchicCursor) {
        try {
            streamHandler.stream(rootCursor)
        } finally {
            rootCursor.streamReader.closeCompletely()
        }
    }
}
