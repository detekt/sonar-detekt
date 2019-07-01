package io.gitlab.arturbosch.detekt.sonar.profiles

import io.gitlab.arturbosch.detekt.sonar.foundation.DETEKT_WAY
import io.gitlab.arturbosch.detekt.sonar.foundation.KEY
import io.gitlab.arturbosch.detekt.sonar.rules.ruleKeys
import io.gitlab.arturbosch.detekt.sonar.rules.severityTranslations
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition

class KotlinProfile : BuiltInQualityProfilesDefinition {

    override fun define(context: BuiltInQualityProfilesDefinition.Context) {
        val profile = context.createBuiltInQualityProfile(DETEKT_WAY, KEY)
        profile.isDefault = true

        ruleKeys.filter { it.active }.forEach {
            val severity = severityTranslations[it.issue.severity]
                ?: error("Unexpected severity '${it.issue.severity}'")
            val rule = profile.activateRule(it.repository(), it.rule())
            rule.overrideSeverity(severity)
        }

        profile.done()
    }
}
