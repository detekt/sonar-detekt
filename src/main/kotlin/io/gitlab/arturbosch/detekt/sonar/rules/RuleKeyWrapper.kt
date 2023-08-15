package io.gitlab.arturbosch.detekt.sonar.rules

import io.github.detekt.tooling.api.DefaultConfigurationProvider
import io.github.detekt.tooling.dsl.ExtensionsSpecBuilder
import io.gitlab.arturbosch.detekt.api.BaseRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.sonar.foundation.REPOSITORY_KEY
import org.sonar.api.rule.RuleKey
import java.util.ServiceLoader

internal val extensionSpec = ExtensionsSpecBuilder()
    .build()

internal val defaultConfig: Config = DefaultConfigurationProvider.load(extensionSpec).get()

/**
 * Exclude similar or duplicated rule implementations from other rule sets than the default one.
 */
internal val excludedDuplicates = setOf(
    "Filename", // MatchingDeclarationName
    "FinalNewline", // NewLineAtEndOfFile
    "MaximumLineLength", // MaxLineLength
    "NoUnitReturn", // OptionalUnit
    "NoWildcardImports", // WildcardImport
    "MultiLineIfElse" // MandatoryBracesIfStatements
)

internal val allLoadedRules: List<Rule> =
    ServiceLoader.load(RuleSetProvider::class.java, Config::class.java.classLoader)
        .asSequence()
        .flatMap { loadRules(it).asSequence() }
        .filterIsInstance<Rule>()
        .filterNot { it.ruleId in excludedDuplicates }
        .toList()

private fun loadRules(provider: RuleSetProvider): List<BaseRule> {
    val subConfig = defaultConfig.subConfig(provider.ruleSetId)
    return provider.instance(subConfig).rules
}

internal val ruleKeys: List<RuleKeyWrapper> = allLoadedRules.map { defineRuleKey(it) }

internal val ruleKeyLookup: Map<String, RuleKeyWrapper> = ruleKeys.associateBy { it.ruleId }

internal class RuleKeyWrapper(
    repository: String,
    val issue: Issue,
    val active: Boolean,
) {
    val key: RuleKey = RuleKey.of(repository, issue.id)
    val ruleId: String
        get() = key.rule()
}

private fun defineRuleKey(rule: Rule): RuleKeyWrapper =
    RuleKeyWrapper(REPOSITORY_KEY, rule.issue, rule.active)
