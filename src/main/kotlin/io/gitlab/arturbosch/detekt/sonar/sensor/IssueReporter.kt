package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.sonar.foundation.LOG
import io.gitlab.arturbosch.detekt.sonar.rules.RULE_KEY_LOOKUP
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.issue.NewIssue

/**
 * @author Artur Bosch
 */
class IssueReporter(private val detektion: Detektion,
					private val context: SensorContext) {

	private val fileSystem = context.fileSystem()
	private val baseDir = fileSystem.baseDir()

	fun run() {
		detektion.findings.forEach { ruleSet, findings ->
			LOG.info("RuleSet: $ruleSet - ${findings.size}")
			findings.forEach(this::reportIssue)
		}
	}

	private fun reportIssue(issue: Finding) {
		if (issue.startPosition.line < 0) {
			LOG.info("Invalid location for ${issue.compactWithSignature()}.")
			return
		}
		val pathOfIssue = baseDir.resolve(issue.location.file)
		val inputFile = fileSystem.inputFile(fileSystem.predicates().`is`(pathOfIssue))
		if (inputFile != null) {
			RULE_KEY_LOOKUP[issue.id]?.let {
				val newIssue = context.newIssue()
						.forRule(it)
						.primaryLocation(issue, inputFile)
				newIssue.save()
			} ?: LOG.warn("Could not find rule key for detekt rule ${issue.id} (${issue.compactWithSignature()}).")
		} else {
			LOG.info("No file found for ${issue.location.file}")
		}
	}

	private fun NewIssue.primaryLocation(finding: Finding, inputFile: InputFile): NewIssue {
		val line = finding.startPosition.line
		val metricMessages = finding.metrics
				.joinToString(" ") { "${it.type} ${it.value} is greater than the threshold ${it.threshold}." }
		val newIssueLocation = newLocation()
				.on(inputFile)
				.at(inputFile.selectLine(line))
				.message("${finding.issue.description} $metricMessages")
		return this.at(newIssueLocation)
	}
}
