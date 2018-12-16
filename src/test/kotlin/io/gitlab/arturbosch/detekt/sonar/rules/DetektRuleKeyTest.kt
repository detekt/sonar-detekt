package io.gitlab.arturbosch.detekt.sonar.rules

import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.sonar.api.rule.RuleKey

class DetektRuleKeyTest {

    @Test
    fun `The rule 'DetektRuleKey(*)' does not exist`() {
        var rulesByRuleKey = HashMap<RuleKey, Any?>()

        rulesByRuleKey.put(RuleKey.of("detekt-kotlin", "NewLineAtEndOfFile"), "COUCOU")

        var ruleKey = DetektRuleKey("detekt-kotlin", "NewLineAtEndOfFile", true,
                Issue("NewLineAtEndOfFile", Severity.Style, debt = Debt(mins = 5), description = ""))

        assertThat(rulesByRuleKey).containsKey(ruleKey)
        assertThat(rulesByRuleKey.get(ruleKey)).isNotNull
    }

}