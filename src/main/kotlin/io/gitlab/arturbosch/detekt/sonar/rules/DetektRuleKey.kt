package io.gitlab.arturbosch.detekt.sonar.rules

import io.github.detekt.tooling.api.DefaultConfigurationProvider
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.MultiRule
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.internal.BaseRule
import io.gitlab.arturbosch.detekt.sonar.foundation.REPOSITORY_KEY
import org.sonar.api.rule.RuleKey
import java.util.ServiceLoader

internal val defaultConfig: Config = DefaultConfigurationProvider.load().get()

/**
 * Exclude similar or duplicated rule implementations from other rule sets than the default one.
 */
internal val excludedDuplicates = setOf(
    "Filename", // MatchingDeclarationName
    "MaximumLineLength", // MaxLineLength
    "NoUnitReturn", // OptionalUnit
    "NoWildcardImports", // WildcardImport
    "MultiLineIfElse" // MandatoryBracesIfStatements
)

internal val allLoadedRules: List<Rule> =
    ServiceLoader.load(RuleSetProvider::class.java, Config::class.java.classLoader)
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

internal val ruleKeys: List<DetektRuleKey> = allLoadedRules.map { defineRuleKey(it) }

internal val ruleKeyLookup: Map<String, DetektRuleKey> = ruleKeys.associateBy { it.ruleKey }

internal data class DetektRuleKey(
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
