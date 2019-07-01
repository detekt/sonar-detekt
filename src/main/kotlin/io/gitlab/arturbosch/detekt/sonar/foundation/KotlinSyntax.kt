package io.gitlab.arturbosch.detekt.sonar.foundation

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.highlighting.NewHighlighting
import org.sonar.api.batch.sensor.highlighting.TypeOfText

object KotlinSyntax {

    fun processFile(inputFile: InputFile, ktFile: KtFile, context: SensorContext) {
        val syntax = context.newHighlighting().onFile(inputFile)

        ktFile.node.visitTokens {
            when (it.elementType) {
                in KtTokens.KEYWORDS -> syntax.highlightByType(it, TypeOfText.KEYWORD)
                in KtTokens.SOFT_KEYWORDS -> syntax.highlightByType(it, TypeOfText.KEYWORD)
                in KtTokens.STRINGS -> syntax.highlightByType(it, TypeOfText.STRING)
                in KtTokens.COMMENTS -> syntax.highlightByType(it, TypeOfText.COMMENT)
                KtTokens.OPEN_QUOTE, KtTokens.CLOSING_QUOTE -> syntax.highlightByType(it, TypeOfText.STRING)
                KtTokens.AT -> syntax.handleAnnotations(it)
                KtTokens.SHORT_TEMPLATE_ENTRY_START, KtTokens.LONG_TEMPLATE_ENTRY_START, KtTokens.ESCAPE_SEQUENCE,
                KtTokens.LONG_TEMPLATE_ENTRY_END -> syntax.highlightByType(it, TypeOfText.ANNOTATION)
            }
        }

        syntax.save()
    }

    private fun ASTNode.visitTokens(currentNode: (node: ASTNode) -> Unit) {
        currentNode(this)
        getChildren(null).forEach { it.visitTokens(currentNode) }
    }

    private fun NewHighlighting.highlightByType(astNode: ASTNode, type: TypeOfText) {
        val range = astNode.textRange
        highlight(range.startOffset, range.endOffset, type)
    }

    private fun NewHighlighting.handleAnnotations(astNode: ASTNode) {
        val psi = astNode.psi
        val annotationEntry = psi.getNonStrictParentOfType(KtAnnotationEntry::class.java)
        if (annotationEntry != null) {
            val range = annotationEntry.textRange
            highlight(range.startOffset, range.endOffset, TypeOfText.ANNOTATION)
        }
    }
}
