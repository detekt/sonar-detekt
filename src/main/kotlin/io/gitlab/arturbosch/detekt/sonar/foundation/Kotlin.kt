package io.gitlab.arturbosch.detekt.sonar.foundation

import io.gitlab.arturbosch.detekt.sonar.DetektPlugin
import org.sonar.api.utils.log.Logger
import org.sonar.api.utils.log.Loggers
import java.util.Optional

const val KEY = "kotlin"
const val NAME = "Kotlin"
const val FILE_SUFFIX = ".kt"
const val SCRIPT_SUFFIX = ".kts"

const val DETEKT_WAY = "detekt active"
const val DETEKT_FAIL_FAST = "detekt all"
const val DETEKT_SENSOR = "DetektSensor"
const val DETEKT_REPOSITORY = "detekt-kotlin"
const val DETEKT_ANALYZER = "Detekt-based Kotlin Analyzer"

val logger: Logger = Loggers.get(DetektPlugin::class.java)

fun <T> Optional<T>.unwrap(): T? = orElse(null)
