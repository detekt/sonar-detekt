package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.sonar.foundation.logger
import io.gitlab.arturbosch.detekt.sonar.rules.excludedDuplicates
import io.gitlab.arturbosch.detekt.sonar.rules.ruleKeyLookup
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.issue.NewIssue

class IssueReporter(
    private val result: Detektion,
    private val context: SensorContext
) {

    private val fileSystem = context.fileSystem()
    private val baseDir = fileSystem.baseDir()

    fun run() {
        for ((ruleSet, findings) in result.findings) {
            logger.info("RuleSet: $ruleSet - ${findings.size}")
            findings.forEach(this::reportIssue)
        }
    }

    private fun reportIssue(issue: Finding) {
        if (issue.id in excludedDuplicates) {
            return
        }
        if (issue.startPosition.line < 0) {
            logger.info("Invalid location for ${issue.compactWithSignature()}.")
            return
        }
        val pathOfIssue = baseDir.resolveSibling(issue.location.filePath.absolutePath.toString())
        val inputFile = fileSystem.inputFile(fileSystem.predicates().`is`(pathOfIssue))
        if (inputFile != null) {
            ruleKeyLookup[issue.id]?.let {
                val newIssue = context.newIssue()
                    .forRule(it)
                    .primaryLocation(issue, inputFile)
                newIssue.save()
            } ?: logger.warn("Could not find rule key for detekt rule ${issue.id} (${issue.compactWithSignature()}).")
        } else {
            logger.info("No file found for ${issue.location.filePath.absolutePath.toString()}")
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
