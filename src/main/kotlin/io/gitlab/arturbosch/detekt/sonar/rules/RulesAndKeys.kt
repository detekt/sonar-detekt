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
import io.gitlab.arturbosch.detekt.sonar.foundation.LOG
import org.sonar.api.rule.RuleKey
import java.util.ServiceLoader

val DEFAULT_YAML_CONFIG = YamlConfig.loadResource(
		ClasspathResourceConverter().convert("default-detekt-config.yml")).apply {
	LOG.info(this.toString())
}

val ALL_LOADED_RULES = ServiceLoader.load(RuleSetProvider::class.java, Config::javaClass.javaClass.classLoader)
		.flatMap { loadRules(it) }
		.flatMap { (it as? MultiRule)?.rules ?: listOf(it) }
		.filterIsInstance<Rule>()
		.toList()

private fun loadRules(provider: RuleSetProvider): List<BaseRule> {
	val subConfig = DEFAULT_YAML_CONFIG.subConfig(provider.ruleSetId)
	return provider.instance(subConfig).rules
}

val RULE_KEYS = ALL_LOADED_RULES.map { defineRuleKey(it) }

val RULE_KEY_LOOKUP = RULE_KEYS.map { it.ruleKey to it }.toMap()

data class DetektRuleKey(val repositoryKey: String,
						 val ruleKey: String,
						 val active: Boolean,
						 val issue: Issue) : RuleKey(repositoryKey, ruleKey)

private fun defineRuleKey(rule: Rule) = DetektRuleKey(DETEKT_REPOSITORY, rule.id, rule.active, rule.issue)
