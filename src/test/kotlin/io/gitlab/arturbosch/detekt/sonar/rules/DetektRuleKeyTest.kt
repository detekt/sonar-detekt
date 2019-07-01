package io.gitlab.arturbosch.detekt.sonar.rules

import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.sonar.api.rule.RuleKey

class DetektRuleKeyTest {

    @Test
    fun `The rule 'DetektRuleKey(*)' does not exist`() {
        val rulesByRuleKey = HashMap<RuleKey, Any?>()

        rulesByRuleKey[RuleKey.of("detekt-kotlin", "NewLineAtEndOfFile")] = "COUCOU"

        val ruleKey = DetektRuleKey("detekt-kotlin", "NewLineAtEndOfFile", true,
            Issue("NewLineAtEndOfFile", Severity.Style, debt = Debt(mins = 5), description = ""))

        assertThat(rulesByRuleKey).containsKey(ruleKey)
        assertThat(rulesByRuleKey[ruleKey]).isNotNull
    }
}
