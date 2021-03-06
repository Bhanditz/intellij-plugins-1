package org.stepik.core

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import javax.swing.JPanel

abstract class StudyBasePluginConfigurator : StudyPluginConfigurator {
    
    override fun getActionGroup(project: Project): DefaultActionGroup {
        return defaultActionGroup
    }
    
    override fun getAdditionalPanels(project: Project): Map<String, JPanel> {
        return emptyMap()
    }
    
    override fun getFileEditorManagerListener(project: Project): FileEditorManagerListener {
        return object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                val file = event.newFile ?: return
                
                getProjectManager(project)?.selected = getStudyNode(project, file) ?: return
            }
        }
    }
    
    companion object {
        val defaultActionGroup: DefaultActionGroup
            get() = DefaultActionGroup()
    }
}
