package io.gitlab.arturbosch.detekt.sonar.sensor

import io.gitlab.arturbosch.detekt.api.Detektion
import io.gitlab.arturbosch.detekt.api.FileProcessListener
import io.gitlab.arturbosch.detekt.api.SingleAssign
import org.jetbrains.kotlin.psi.KtFile

class ReturnKtFilesAnalysisEndsListener : FileProcessListener {

    var kotlinFiles: List<KtFile> by SingleAssign()

    override fun onFinish(files: List<KtFile>, result: Detektion) {
        kotlinFiles = files
    }
}
