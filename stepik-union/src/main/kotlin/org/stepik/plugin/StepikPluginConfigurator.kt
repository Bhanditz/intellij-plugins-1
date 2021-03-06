package org.stepik.plugin

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.stepik.core.ProjectGenerator
import org.stepik.core.StudyBasePluginConfigurator
import org.stepik.core.StudyPluginConfigurator
import org.stepik.core.actions.navigation.StudyNavigator
import org.stepik.core.actions.step.DownloadSubmission
import org.stepik.core.actions.step.InsertStepikDirectives
import org.stepik.core.actions.step.StepikResetStepAction
import org.stepik.core.actions.step.TestSamplesAction
import org.stepik.core.courseFormat.StepNode
import org.stepik.core.courseFormat.StudyNode
import org.stepik.core.isStepikProject
import org.stepik.core.projectWizard.idea.BaseModuleBuilder
import org.stepik.plugin.actions.navigation.StepikNextStepAction
import org.stepik.plugin.actions.navigation.StepikPreviousStepAction
import org.stepik.plugin.actions.step.OpenInBrowserAction
import org.stepik.plugin.actions.step.StepikSendAction
import org.stepik.plugin.projectWizard.StepikProjectGenerator
import org.stepik.plugin.projectWizard.idea.SandboxModuleBuilder
import org.stepik.plugin.projectWizard.idea.StepModuleBuilder

class StepikPluginConfigurator : StudyBasePluginConfigurator() {
    
    override val pluginDescription = "Please, open any step to see step description"
    
    override val nextButtonCaption = "Next Step"
    
    override fun getSandboxModuleBuilder(path: String): BaseModuleBuilder? {
        return SandboxModuleBuilder(path)
    }
    
    override fun getStepModuleBuilder(moduleDir: String, step: StepNode): BaseModuleBuilder? {
        return StepModuleBuilder(moduleDir, step)
    }
    
    override fun getProjectGenerator(): ProjectGenerator? {
        return StepikProjectGenerator
    }
    
    override fun nextAction(project: Project, node: StepNode?): StudyNode? {
        return StudyNavigator.nextLeaf(node)
    }
    
    override fun enabledNextAction(project: Project, currentNode: StepNode?): Boolean {
        return StudyNavigator.nextLeaf(currentNode) != null
    }
    
    override fun getActionGroup(project: Project): DefaultActionGroup {
        val group = DefaultActionGroup()
        group.addAll(
                StepikSendAction(),
                TestSamplesAction(),
                StepikPreviousStepAction(),
                StepikNextStepAction(),
                StepikResetStepAction(),
                DownloadSubmission(),
                InsertStepikDirectives(),
                OpenInBrowserAction()
        )
        
        return group
    }
    
    override fun accept(project: Project): Boolean {
        return isStepikProject(project)
    }
    
    companion object {
        val EP_NAME = ExtensionPointName.create<StudyPluginConfigurator>(
                "org.stepik.plugin.union.studyPluginConfigurator")
        
    }
}
