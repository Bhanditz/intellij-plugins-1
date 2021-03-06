package org.stepik.core.testFramework.runners

import com.intellij.execution.RunManager
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import org.stepik.core.EduNames
import org.stepik.core.common.Loggable
import org.stepik.core.courseFormat.StepNode
import org.stepik.core.testFramework.createDirectories
import org.stepik.core.testFramework.processes.TestProcess
import org.stepik.core.utils.getTextUnderDirectives
import org.stepik.core.utils.replaceCode
import org.stepik.core.utils.runWriteActionAndWait
import java.io.IOException
import java.util.concurrent.TimeUnit

interface Runner : Loggable {

    fun updateRunConfiguration(project: Project, stepNode: StepNode) {
        getApplication().invokeLater {
            RunManager.getInstance(project).selectedConfiguration = null
        }
    }

    fun createTestProcess(project: Project, stepNode: StepNode, mainFilePath: String) =
            TestProcess(project, stepNode, mainFilePath)

    fun testSamples(project: Project,
                    stepNode: StepNode,
                    input: String,
                    assertion: (String) -> Boolean,
                    mainFilePath: String? = null,
                    testClass: Boolean = false): TestResult {
        val mainFile = mainFilePath ?: getMainFilePath(project, stepNode)?.path ?: return NO_PROCESS
        val process = createTestProcess(project, stepNode, mainFile).start(testClass) ?: return NO_PROCESS

        writeToProcessInput(process, input)

        val success = process.waitFor(stepNode.limit.time.toLong(), TimeUnit.SECONDS)

        if (!success) {
            process.destroyForcibly()
            return TIME_LEFT
        }

        val actual = process.inputStream.bufferedReader().readText()
        val errorString = process.errorStream.bufferedReader().readText()

        val score = process.exitValue() == 0 && assertion(actual)
        val cause = ExitCause.of(score)
        return TestResult(score, actual, cause, errorString)
    }

    fun writeToProcessInput(process: Process, input: String) {
        process.outputStream.bufferedWriter().apply {
            write("$input\n")
            flush()
        }
    }

    fun getMainFilePath(project: Project, stepNode: StepNode): VirtualFile? {
        return project.baseDir.findFileByRelativePath(stepNode.path)
                ?.findChild(EduNames.SRC)
                ?.findChild(stepNode.currentLang.mainFileName)
                ?: return null
    }

    fun testFiles(project: Project, stepNode: StepNode): TestResult {
        val targetTestFile = prepareMainFile(project, stepNode) ?: return NO_PROCESS
        return testSamples(project, stepNode, "", { it.toBoolean() }, targetTestFile, true)
    }

    private fun prepareMainFile(project: Project, stepNode: StepNode): String? {
        val language = stepNode.currentLang

        val testFileName = language.testFileName

        val stepDirectory = project.baseDir.findFileByRelativePath(stepNode.path) ?: return null

        val testFile = stepDirectory.findFileByRelativePath(listOf("tests", language.langName)
                .joinToString("/"))
                ?: stepDirectory.findChild(testFileName)

        val mainFile = getMainFilePath(project, stepNode) ?: return null

        val targetFileDirectory = createDirectories(stepDirectory,
                "out/${language.langName}") ?: return null
        val targetFilePath = targetFileDirectory.findChild(testFileName)
                ?: createFile(targetFileDirectory, testFileName) ?: return null

        val documentManager = FileDocumentManager.getInstance()
        val text = getApplication().runReadAction(Computable<String> {
            val submissionText = documentManager.getDocument(mainFile)?.text ?: return@Computable null
            val submission = getTextUnderDirectives(submissionText, language)
            if (testFile != null) {
                val testText = documentManager.getDocument(testFile)?.text ?: return@Computable null
                replaceCode(testText, submission, language)
            } else {
                submission
            }
        }) ?: return null

        writeText(documentManager, targetFilePath, text)

        return targetFilePath.path
    }

    fun writeText(documentManager: FileDocumentManager, targetFilePath: VirtualFile, text: String) {
        getApplication().runWriteActionAndWait {
            val document = documentManager.getDocument(targetFilePath) ?: return@runWriteActionAndWait
            document.setText(text)
            documentManager.saveDocument(document)
        }
    }

    fun createFile(parent: VirtualFile, testFileName: String): VirtualFile? {
        return getApplication().runWriteActionAndWait {
            return@runWriteActionAndWait try {
                parent.findOrCreateChildData(null, testFileName)
            } catch (e: IOException) {
                null
            }
        }
    }
}
