package io.gitlab.arturbosch.detekt.sonar.rules

import io.gitlab.arturbosch.detekt.api.BaseRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.MultiRule
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.YamlConfig
import io.gitlab.arturbosch.detekt.cli.ClasspathResourceConverter
import io.gitlab.arturbosch.detekt.sonar.foundation.DETEKT_REPOSITORY
import io.gitlab.arturbosch.detekt.sonar.foundation.logger
import org.sonar.api.rule.RuleKey
import java.util.ServiceLoader

val defaultYamlConfig = YamlConfig.loadResource(
		ClasspathResourceConverter().convert("default-detekt-config.yml")).apply {
	logger.info(this.toString())
}

val allLoadedRules = ServiceLoader.load(RuleSetProvider::class.java, Config::javaClass.javaClass.classLoader)
		.flatMap { loadRules(it) }
		.flatMap { (it as? MultiRule)?.rules ?: listOf(it) }
		.asSequence()
		.filterIsInstance<Rule>()
		.toList()

private fun loadRules(provider: RuleSetProvider): List<BaseRule> {
	val subConfig = defaultYamlConfig.subConfig(provider.ruleSetId)
	return provider.instance(subConfig).rules
}

val ruleKeys = allLoadedRules.map { defineRuleKey(it) }

val ruleKeyLookup = ruleKeys.map { it.ruleKey to it }.toMap()

data class DetektRuleKey(private val repositoryKey: String,
						 val ruleKey: String,
						 val active: Boolean,
						 val issue: Issue) : RuleKey(repositoryKey, ruleKey)

private fun defineRuleKey(rule: Rule) = DetektRuleKey(DETEKT_REPOSITORY, rule.ruleId, rule.active, rule.issue)
