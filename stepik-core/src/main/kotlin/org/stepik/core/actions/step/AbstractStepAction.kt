package org.stepik.core.actions.step

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Contract
import org.stepik.core.actions.StudyActionWithShortcut
import org.stepik.core.courseFormat.StepNode
import org.stepik.core.getProjectManager
import javax.swing.Icon

abstract class AbstractStepAction protected constructor(text: String?, description: String?, icon: Icon?) :
        StudyActionWithShortcut(text, description, icon) {
    
    override fun update(e: AnActionEvent?) {
        val presentation = e?.presentation ?: return
        val stepNode = getCurrentStep(e.project)
        presentation.isEnabled = stepNode != null && !stepNode.wasDeleted
    }
    
    companion object {
        @Contract("null -> null")
        fun getCurrentStep(project: Project?): StepNode? {
            if (project == null) {
                return null
            }
            val projectManager = getProjectManager(project)
            return projectManager?.selected as? StepNode
        }
    }
}
