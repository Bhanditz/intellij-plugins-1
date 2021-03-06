package org.stepik.core.testFramework.runners

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.stepik.core.EduNames
import org.stepik.core.courseFormat.StepNode
import org.stepik.core.utils.runWriteActionAndWait

abstract class JetRunner : Runner {
    
    override fun updateRunConfiguration(project: Project, stepNode: StepNode) {
        val runManager = RunManager.getInstance(project) as RunManagerImpl
        val language = stepNode.currentLang
        
        val settingName = "${stepNode.parent?.name ?: "Lesson"} | step ${stepNode.position} ($language)"
        
        val runConfiguration = getRunConfiguration(runManager, settingName, stepNode)
        if (runConfiguration == null) {
            setConfiguration(runManager)
            return
        }
        
        val appConfiguration = runConfiguration.configuration
        val workingVirtualDirectory = project.baseDir.findFileByRelativePath(stepNode.path) ?: return
        setWorkingDirectory(appConfiguration, workingVirtualDirectory)
        val mainRelativePath = listOf(EduNames.SRC, language.mainFileName).joinToString("/")
        val mainVirtualFile = workingVirtualDirectory.findFileByRelativePath(mainRelativePath)
        setMainClass(project, appConfiguration, mainVirtualFile)
        setModule(project, appConfiguration, mainVirtualFile)
        setSdk(project, appConfiguration, mainVirtualFile)
        setConfiguration(runManager, runConfiguration)
    }
    
    private fun getRunConfiguration(runManager: RunManagerImpl, settingName: String,
                                    stepNode: StepNode): RunnerAndConfigurationSettings? {
        return null
    }
    
    protected abstract val typeName: String
    
    protected abstract val factoryName: String
    
    protected abstract fun setWorkingDirectory(appConfiguration: RunConfiguration,
                                               workingVirtualDirectory: VirtualFile)
    
    protected abstract fun setMainClass(project: Project,
                                        appConfiguration: RunConfiguration,
                                        mainVirtualFile: VirtualFile?)
    
    private fun setModule(project: Project, appConfiguration: RunConfiguration,
                          mainVirtualFile: VirtualFile?) {
        if (mainVirtualFile != null && appConfiguration is ModuleBasedConfiguration<*>) {
            getApplication().runWriteActionAndWait {
                val module = ModuleUtilCore.findModuleForFile(mainVirtualFile, project)
                appConfiguration.setModule(module)
            }
        }
    }
    
    protected open fun setSdk(project: Project, appConfiguration: RunConfiguration,
                              mainVirtualFile: VirtualFile?) = Unit
    
    private fun setConfiguration(runManager: RunManager,
                                 configuration: RunnerAndConfigurationSettings? = null) {
        getApplication().invokeLater { runManager.selectedConfiguration = configuration }
    }
}
