package io.gitlab.arturbosch.detekt.sonar.rules

import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.sonar.foundation.DETEKT_ANALYZER
import io.gitlab.arturbosch.detekt.sonar.foundation.LANGUAGE_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.REPOSITORY_KEY
import org.sonar.api.rule.RuleStatus
import org.sonar.api.server.rule.RulesDefinition
import java.util.Locale

class DetektRulesDefinition : RulesDefinition {

    override fun define(context: RulesDefinition.Context) {
        context.createRepository(REPOSITORY_KEY, LANGUAGE_KEY)
            .setName(DETEKT_ANALYZER)
            .createRules()
            .done()
    }
}

private fun RulesDefinition.NewRepository.createRules(): RulesDefinition.NewRepository =
    apply { allLoadedRules.map { defineRule(it) } }

private fun RulesDefinition.NewRepository.defineRule(rule: Rule) {
    val description = rule.issue.description
    check(description.isNotBlank()) { "Rule '${rule.ruleId}' has no description." }
    val severity = severityTranslations[rule.issue.severity]
    checkNotNull(severity) { "Unexpected severity '${rule.issue.severity}' for rule '${rule.ruleId}'." }
    val newRule = createRule(rule.ruleId).setName(rule.ruleId)
        .setHtmlDescription(description)
        .setTags(rule.issue.severity.name.lowercase(Locale.getDefault()))
        .setStatus(RuleStatus.READY)
        .setSeverity(severity)
    newRule.setDebtRemediationFunction(
        newRule.debtRemediationFunctions().linear(rule.issue.debt.toString())
    )
}
