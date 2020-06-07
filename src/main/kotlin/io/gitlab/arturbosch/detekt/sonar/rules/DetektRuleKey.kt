package io.gitlab.arturbosch.detekt.sonar.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.MultiRule
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.internal.BaseRule
import io.gitlab.arturbosch.detekt.cli.loadDefaultConfig
import io.gitlab.arturbosch.detekt.sonar.foundation.REPOSITORY_KEY
import org.sonar.api.rule.RuleKey
import java.util.ServiceLoader

val defaultConfig: Config = loadDefaultConfig()

val excludedDuplicates = setOf(
    "Filename", // from KtLint; same as MatchingDeclarationName
    "MaximumLineLength", // from KtLint; same as MaxLineLength
    "NoUnitReturn", // from KtLint; same as OptionalUnit
    "NoWildcardImports" // from KtLint; same as WildcardImport
)

val allLoadedRules: List<Rule> = ServiceLoader.load(RuleSetProvider::class.java, Config::class.java.classLoader)
    .asSequence()
    .flatMap { loadRules(it).asSequence() }
    .flatMap { (it as? MultiRule)?.rules?.asSequence() ?: sequenceOf(it) }
    .filterIsInstance<Rule>()
    .filterNot { it.ruleId in excludedDuplicates }
    .toList()

private fun loadRules(provider: RuleSetProvider): List<BaseRule> {
    val subConfig = defaultConfig.subConfig(provider.ruleSetId)
    return provider.instance(subConfig).rules
}

val ruleKeys: List<DetektRuleKey> = allLoadedRules.map { defineRuleKey(it) }

val ruleKeyLookup: Map<String, DetektRuleKey> = ruleKeys.associateBy { it.ruleKey }

data class DetektRuleKey(
    private val repositoryKey: String,
    val ruleKey: String,
    val active: Boolean,
    val issue: Issue
) : RuleKey(repositoryKey, ruleKey) {
    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other?.javaClass != javaClass && other?.javaClass != RuleKey::class.java) return false

        val ruleKey = other as RuleKey
        return repository() == ruleKey.repository() && rule() == ruleKey.rule()
    }
}

private fun defineRuleKey(rule: Rule): DetektRuleKey =
    DetektRuleKey(REPOSITORY_KEY, rule.ruleId, rule.active, rule.issue)
