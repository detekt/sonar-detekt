package io.gitlab.arturbosch.detekt.sonar.foundation

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.editor.Document
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.highlighting.TypeOfText

object KotlinSyntax {

    @Suppress("ComplexMethod", "TooGenericExceptionCaught")
    fun processFile(inputFile: InputFile, ktFile: KtFile, context: SensorContext) {
        val syntax = context.newHighlighting().onFile(inputFile)
        val document: Document = ktFile.viewProvider.document ?: return

        fun positions(psi: PsiElement) =
            PsiDiagnosticUtils.offsetToLineAndColumn(document, psi.textRange.startOffset) to
                PsiDiagnosticUtils.offsetToLineAndColumn(document, psi.textRange.endOffset)

        fun highlightByType(psi: PsiElement, type: TypeOfText) {
            try {
                val (start, end) = positions(psi)
                syntax.highlight(
                    inputFile.newRange(
                        start.line,
                        start.column - 1,
                        end.line,
                        end.column - 1
                    ),
                    type
                )
            } catch (error: Throwable) {
                logger.warn(
                    "Could not highlight psi element '$psi'" +
                        " with content '${psi.text}'" +
                        " for file '${inputFile.uri()}'."
                )
                logger.warn(error.localizedMessage)
            }
        }

        fun highlightByType(astNode: ASTNode, type: TypeOfText) {
            val psi = astNode.psi
            if (psi != null) {
                highlightByType(psi, type)
            }
        }

        fun handleAnnotation(astNode: ASTNode) {
            val annotationEntry = astNode.psi
                ?.getNonStrictParentOfType(KtAnnotationEntry::class.java)
            if (annotationEntry != null) {
                highlightByType(annotationEntry, TypeOfText.ANNOTATION)
            }
        }

        ktFile.node.visitTokens {
            when (it.elementType) {
                in KtTokens.KEYWORDS -> highlightByType(it, TypeOfText.KEYWORD)
                in KtTokens.SOFT_KEYWORDS -> highlightByType(it, TypeOfText.KEYWORD)
                in KtTokens.STRINGS -> highlightByType(it, TypeOfText.STRING)
                in KtTokens.COMMENTS -> highlightByType(it, TypeOfText.COMMENT)
                KtTokens.OPEN_QUOTE, KtTokens.CLOSING_QUOTE -> highlightByType(it, TypeOfText.STRING)
                KtTokens.AT -> handleAnnotation(it)
                KtTokens.SHORT_TEMPLATE_ENTRY_START, KtTokens.LONG_TEMPLATE_ENTRY_START, KtTokens.ESCAPE_SEQUENCE,
                KtTokens.LONG_TEMPLATE_ENTRY_END -> highlightByType(it, TypeOfText.ANNOTATION)
            }
        }

        syntax.save()
    }

    private fun ASTNode.visitTokens(currentNode: (node: ASTNode) -> Unit) {
        currentNode(this)
        getChildren(null).forEach { it.visitTokens(currentNode) }
    }
}
