package org.stepik.plugin.actions.step

import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.project.Project
import org.stepik.core.actions.step.AbstractOpenInBrowserAction
import org.stepik.core.courseFormat.StudyNode
import org.stepik.core.host
import org.stepik.plugin.StepikProjectManager

class OpenInBrowserAction : AbstractOpenInBrowserAction() {
    
    override fun getLink(project: Project, stepNode: StudyNode): String {
        val parent = stepNode.parent ?: return host
        val link = "$host/lesson/${parent.id}/step/${stepNode.position}"
        val projectManager = getService(project, StepikProjectManager::class.java)
        if (projectManager?.isAdaptive == true) {
            return "$link?adaptive=true"
        }
        return link
    }
    
}
