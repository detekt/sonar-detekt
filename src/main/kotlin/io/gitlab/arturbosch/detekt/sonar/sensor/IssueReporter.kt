package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.cli.baseline.BaselineFacade
import io.gitlab.arturbosch.detekt.sonar.foundation.BASELINE_KEY
import io.gitlab.arturbosch.detekt.sonar.foundation.logger
import io.gitlab.arturbosch.detekt.sonar.rules.ruleKeyLookup
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.issue.NewIssue
import org.sonar.api.config.Settings
import java.io.File

/**
 * @author Artur Bosch
 */
class IssueReporter(private val detektion: Detektion,
					private val context: SensorContext) {

	private val fileSystem = context.fileSystem()
	private val baseDir = fileSystem.baseDir()
	private val settings = context.settings()

	fun run() {
		detektion.findings.forEach { ruleSet, findings ->
			logger.info("RuleSet: $ruleSet - ${findings.size}")
			val baseline = tryFindBaseline(settings, baseDir)
			val filtered = baseline?.filter(findings) ?: findings
			filtered.forEach(this::reportIssue)
		}
	}

	private fun tryFindBaseline(settings: Settings, baseDir: File): BaselineFacade? {
		return settings.getString(BASELINE_KEY)?.let { path ->
			logger.info("Registered baseline path: $path")
			var baselinePath = File(path)

			if (!baselinePath.exists()) {
				baselinePath = File(baseDir.path, path)
			}

			if (!baselinePath.exists()) {
				val parentFile = baseDir.parentFile
				if (parentFile != null) {
					baselinePath = File(parentFile.path, path)
				} else {
					return null
				}
			}
			BaselineFacade(baselinePath.toPath())
		}
	}

	private fun reportIssue(issue: Finding) {
		if (issue.startPosition.line < 0) {
			logger.info("Invalid location for ${issue.compactWithSignature()}.")
			return
		}
		val pathOfIssue = baseDir.resolveSibling(issue.location.file)
		val inputFile = fileSystem.inputFile(fileSystem.predicates().`is`(pathOfIssue))
		if (inputFile != null) {
			ruleKeyLookup[issue.id]?.let {
				val newIssue = context.newIssue()
						.forRule(it)
						.primaryLocation(issue, inputFile)
				newIssue.save()
			} ?: logger.warn("Could not find rule key for detekt rule ${issue.id} (${issue.compactWithSignature()}).")
		} else {
			logger.info("No file found for ${issue.location.file}")
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
