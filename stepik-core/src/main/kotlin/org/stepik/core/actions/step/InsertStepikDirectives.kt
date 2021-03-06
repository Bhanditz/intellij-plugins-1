package org.stepik.core.actions.step

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.stepik.core.actions.getShortcutText
import org.stepik.core.getProjectManager
import org.stepik.core.metrics.Metrics
import org.stepik.core.pluginId
import org.stepik.core.utils.containsDirectives
import org.stepik.core.utils.getFileText
import org.stepik.core.utils.getOrCreateSrcDirectory
import org.stepik.core.utils.insertAmbientCode
import org.stepik.core.utils.reformatDocument
import org.stepik.core.utils.removeAmbientCode
import org.stepik.core.utils.saveAllDocuments
import org.stepik.core.utils.writeTextToFile

class InsertStepikDirectives : CodeQuizAction(TEXT, DESCRIPTION, AllIcons.General.ExternalToolsSmall) {

    override fun getActionId() = ACTION_ID

    override fun getShortcuts() = arrayOf(SHORTCUT)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        project.saveAllDocuments()

        val stepNode = getCurrentCodeStepNode(project) ?: return

        val src = getOrCreateSrcDirectory(project, stepNode, true) ?: return

        val currentLang = stepNode.currentLang

        val file = src.findChild(currentLang.mainFileName) ?: return

        var text = getFileText(file)

        val showHint = getProjectManager(project)?.showHint ?: false

        val needInsert = !containsDirectives(text, currentLang)

        if (needInsert) {
            text = insertAmbientCode(text, currentLang, showHint)
            Metrics.insertAmbientCodeAction(project, stepNode)
        } else {
            text = removeAmbientCode(text, showHint, currentLang, true)
            Metrics.removeAmbientCodeAction(project, stepNode)
        }

        writeTextToFile(text, file, project)

        if (needInsert) {
            val document = FileDocumentManager.getInstance()
                    .getDocument(file)
            if (document != null) {
                project.reformatDocument(document)
            }
        }

        currentLang.runner.updateRunConfiguration(project, stepNode)
    }

    companion object {
        private const val SHORTCUT = "ctrl alt pressed R"
        private val ACTION_ID = "$pluginId.InsertStepikDirectives"
        private val SHORTCUT_TEXT = getShortcutText(SHORTCUT)
        private val TEXT = "Repair standard template ($SHORTCUT_TEXT)"
        private const val DESCRIPTION = "Insert Stepik directives. Repair ordinary template if it is possible."
    }
}
