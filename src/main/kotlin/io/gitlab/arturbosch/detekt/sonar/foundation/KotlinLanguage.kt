package io.gitlab.arturbosch.detekt.sonar.foundation

import org.sonar.api.resources.AbstractLanguage

class KotlinLanguage : AbstractLanguage(KEY, NAME) {

    override fun getFileSuffixes(): Array<String> = arrayOf(FILE_SUFFIX, SCRIPT_SUFFIX)
}
