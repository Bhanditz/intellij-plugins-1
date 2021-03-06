package org.stepik.core.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import org.stepik.core.auth.StepikAuthManager
import org.stepik.core.auth.StepikAuthManager.isAuthenticated
import org.stepik.core.pluginId

class LogoutAction : StudyActionWithShortcut(TEXT, DESCRIPTION) {
    override fun actionPerformed(e: AnActionEvent?) {
        StepikAuthManager.logout()
    }
    
    override fun getActionId() = ACTION_ID
    
    override fun getShortcuts() = emptyArray<String>()
    
    override fun update(e: AnActionEvent?) {
        e?.presentation?.isVisible = isAuthenticated
    }
    
    companion object {
        private val ACTION_ID = "$pluginId.LogoutAction"
        private const val TEXT = "Logout"
        private const val DESCRIPTION = "Logout"
    }
}
