package org.stepik.plugin.actions.navigation

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import org.stepik.core.actions.getShortcutText
import org.stepik.core.actions.navigation.StudyNavigator.nextLeaf
import org.stepik.core.courseFormat.StudyNode
import org.stepik.core.pluginId

class StepikNextStepAction : StepikNavigationAction(TEXT, DESCRIPTION, AllIcons.Actions.Forward) {
    
    override fun getTargetStep(project: Project, currentStepNode: StudyNode?): StudyNode? {
        return nextLeaf(currentStepNode)
    }
    
    override fun getActionId() = ACTION_ID
    
    override fun getShortcuts() = arrayOf(SHORTCUT)
    
    companion object {
        private val ACTION_ID = "$pluginId.NextStepAction"
        private const val SHORTCUT = "ctrl pressed PERIOD"
        private val SHORTCUT_TEXT = getShortcutText(SHORTCUT)
        private val TEXT = "Next Step ($SHORTCUT_TEXT)"
        private const val DESCRIPTION = "Navigate to the next step"
    }
}
