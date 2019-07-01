package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.FileProcessListener
import org.jetbrains.kotlin.psi.KtFile

class ReturnKtFilesAnalysisEndsListener : FileProcessListener {

    lateinit var kotlinFiles: List<KtFile>

    override fun onFinish(files: List<KtFile>, result: Detektion) {
        kotlinFiles = files
    }
}
