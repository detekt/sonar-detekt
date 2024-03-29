package io.gitlab.arturbosch.detekt.sonar.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.MultiRule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.formatting.FormattingProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.sonar.api.rule.RuleKey

class RuleKeyWrapperTest {

    @Test
    fun `wrapper saves additional information and is not instance of RuleKey`() {
        val ruleKey = RuleKeyWrapper(
            "detekt-kotlin",
            Issue("NewLineAtEndOfFile", Severity.Style, debt = Debt.FIVE_MINS, description = ""),
            true,
        )
        assertThat(ruleKey).isNotInstanceOf(RuleKey::class.java)
        assertThat(ruleKey.key).isEqualTo(RuleKey.of("detekt-kotlin", "NewLineAtEndOfFile"))
    }

    @Test
    fun `loaded rules do not contain duplicates rules from KtLint`() {
        val ids = allLoadedRules.map { it.ruleId }

        assertThat(ids).doesNotContainSequence(excludedDuplicates)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `loaded multi rules need to be extracted first`() {
        val multiRule = FormattingProvider()
            .instance(Config.empty)
            .rules
            .first() as MultiRule

        val expectedFormattingRules = multiRule.rules
            .map { it.ruleId }
            .filterNot { it in excludedDuplicates }

        assertThat(allLoadedRules.map { it.ruleId })
            .containsAll(expectedFormattingRules)
    }
}
