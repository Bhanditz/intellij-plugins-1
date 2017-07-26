package org.stepik.core.testFramework.runners

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.packaging.PyPackageManager
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.run.PythonRunConfiguration
import com.jetbrains.python.sdk.PyDetectedSdk
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.flavors.PythonSdkFlavor
import org.stepik.core.courseFormat.StepNode
import org.stepik.core.testFramework.processes.PythonProcess
import org.stepik.core.testFramework.processes.TestProcess
import org.stepik.core.utils.ProjectFilesUtils

class PythonRunner : JetRunner() {
    override fun getTypeName(): String = "PythonConfigurationType"

    override fun getFactoryName(): String = "Python"

    override fun setWorkingDirectory(appConfiguration: RunConfiguration,
                                     workingVirtualDirectory: VirtualFile) {
        val workingDirectory = workingVirtualDirectory.canonicalPath
        (appConfiguration as PythonRunConfiguration).workingDirectory = workingDirectory
    }

    override fun setMainClass(application: Application,
                              project: Project,
                              appConfiguration: RunConfiguration,
                              mainVirtualFile: VirtualFile?) {
        val module = (appConfiguration as PythonRunConfiguration).module
        val modulePath = module?.moduleFile?.parent?.canonicalPath
        val scriptPath = mainVirtualFile?.canonicalPath
        val scriptRelativePath: String

        if (modulePath != null && scriptPath != null) {
            scriptRelativePath = ProjectFilesUtils.getRelativePath(modulePath, scriptPath)
        } else {
            scriptRelativePath = ""
        }

        appConfiguration.scriptName = scriptRelativePath
    }

    override fun setSdk(project: Project,
                        appConfiguration: RunConfiguration,
                        mainVirtualFile: VirtualFile?) {
        mainVirtualFile ?: return
        val virtualEnvPath = project.basePath + "/.idea/virtualenvs"
        val sdkPath = virtualEnvPath + "/bin/python"
        val baseSdk = getBaseSdk()
        val packageManager = PyPackageManager.getInstance(PyDetectedSdk(baseSdk))
        val path = packageManager.createVirtualEnv(virtualEnvPath, false)
        ApplicationManager.getApplication().invokeAndWait {
            val sdks = ProjectJdkTable.getInstance().getSdksOfType(PythonSdkType.getInstance())
            if (sdks.count { it.homePath == sdkPath } == 0) {
                val sdkType = PythonSdkType.getInstance()
                SdkConfigurationUtil.createAndAddSDK(FileUtil.toSystemDependentName(path), sdkType)
            }
        }
        (appConfiguration as PythonRunConfiguration).isUseModuleSdk = false
        appConfiguration.sdkHome = path
    }

    private fun getBaseSdk(): String {
        val baseLevel = LanguageLevel.PYTHON31
        val flavor = PythonSdkFlavor.getApplicableFlavors(false)[0]
        var baseSdk: String? = null
        val baseSdks = flavor.suggestHomePaths()
        for (sdk in baseSdks) {
            val versionString = flavor.getVersionString(sdk)
            val prefix = flavor.name + " "
            if (versionString != null && versionString.startsWith(prefix)) {
                val level = LanguageLevel.fromPythonVersion(versionString.substring(prefix.length))
                if (level.isAtLeast(baseLevel)) {
                    baseSdk = sdk
                    break
                }
            }
        }
        return if (baseSdk != null) baseSdk else baseSdks.iterator().next()
    }

    override fun createTestProcess(project: Project, stepNode: StepNode, mainFilePath: String): TestProcess {
        return PythonProcess(project, stepNode, mainFilePath)
    }
}