package org.hyperskill.projectWizard.idea

import com.intellij.ide.highlighter.ModuleFileType
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFileManager
import org.stepik.core.courseFormat.StepNode
import org.stepik.core.getProjectManager
import org.stepik.core.projectWizard.ProjectWizardUtils.createSubDirectories
import org.stepik.core.utils.getOrCreateSrcDirectory
import org.hyperskill.projectWizard.StepikProjectGenerator

class CourseModuleBuilder(moduleDir: String? = null) : AbstractModuleBuilder() {
    private val generator = StepikProjectGenerator
    
    init {
        if (moduleDir != null) {
            name = "hyperskill"
            moduleFilePath = FileUtil.join(moduleDir, "hyperskill${ModuleFileType.DOT_DEFAULT_EXTENSION}")
        }
    }
    
    override fun createModule(moduleModel: ModifiableModuleModel): Module {
        val baseModule = super.createModule(moduleModel)
        logger.info("Create project module")
        createHyperskillTreeFromGenerator(moduleModel, baseModule.project)
        return baseModule
    }
    
    private fun createHyperskillTreeFromGenerator(moduleModel: ModifiableModuleModel, project: Project) {
        generator.generateProject(project)
        
        val moduleDir = moduleFileDirectory ?: return
        
        logger.info("Module dir = $moduleDir")
        SandboxModuleBuilder(moduleDir)
                .createModule(moduleModel)
        
        val root = getProjectManager(project)?.projectRoot
        if (root == null) {
            logger.info("Failed to generate builders: project root is null")
            return
        }
        
        if (root is StepNode) {
            getOrCreateSrcDirectory(project, root, true, moduleModel)
        } else {
            createSubDirectories(project, generator.defaultLang, root, moduleModel)
            VirtualFileManager.getInstance()
                    .syncRefresh()
        }
    }
}
