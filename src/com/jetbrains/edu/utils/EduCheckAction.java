package com.jetbrains.edu.utils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.StudyState;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.StudyUtils;
import com.jetbrains.edu.learning.actions.StudyCheckAction;
import com.jetbrains.edu.learning.checker.StudyCheckTask;
import com.jetbrains.edu.learning.checker.StudyCheckUtils;
import com.jetbrains.edu.learning.editor.StudyEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EduCheckAction extends StudyCheckAction {
    private static final Logger LOG = Logger.getInstance(EduCheckAction.class);

    @Override
    public void check(@NotNull Project project) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().runUndoTransparentAction(() -> {
                final StudyEditor selectedEditor = StudyUtils.getSelectedStudyEditor(project);
                if (selectedEditor == null) return;
                final StudyState studyState = new StudyState(selectedEditor);
                if (!studyState.isValid()) {
                    LOG.info("StudyCheckAction was invoked outside study editor");
                    return;
                }
                if (StudyCheckUtils.hasBackgroundProcesses(project)) return;

                ApplicationManager.getApplication().invokeLater(
                        () -> IdeFocusManager.getInstance(project).requestFocus(studyState.getEditor().getComponent(), true));


                VirtualFile taskFileVF = studyState.getVirtualFile();
                Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
                if (sdk == null) {
                    //TODO: create util method for notification
                    return;
                }
                final Course course = StudyTaskManager.getInstance(project).getCourse();
                if (course == null) {
                    return;
                }
                final VirtualFile testsFile = getTestsFile(studyState);
                if (testsFile == null) {
                    return;
                }
                Module module = ModuleUtilCore.findModuleForFile(taskFileVF, project);
                if (module == null) {
                    return;
                }
                CompilerManager.getInstance(project).make(module, (aborted, errors, warnings, compileContext) -> {
                    if (errors != 0) {
                        StudyCheckUtils.showTestResultPopUp("Code has compilation errors", MessageType.WARNING.getPopupBackground(), project);
                        return;
                    }
                    if (aborted) {
                        StudyCheckUtils.showTestResultPopUp("Compilation aborted", MessageType.WARNING.getPopupBackground(), project);
                        return;
                    }
                    RunnerAndConfigurationSettings javaTemplateConfiguration = produceRunConfiguration(project,
                            "javaTemplateConfiguration", ApplicationConfigurationType.getInstance());

                    setProcessParameters(project,
                            ((ApplicationConfiguration) javaTemplateConfiguration.getConfiguration()), taskFileVF, testsFile);

                    RunProfileState state = getState(javaTemplateConfiguration);

                    if (state == null) {
                        //exception is logged inside getState method
                        return;
                    }

                    final JavaCommandLineState javaCmdLine = (JavaCommandLineState) state;
                    FileDocumentManager.getInstance().saveAllDocuments();
                    DumbService.getInstance(project).runWhenSmart(() -> {
                        try {
                            JavaParameters javaParameters;
                            javaParameters = javaCmdLine.getJavaParameters();
                            GeneralCommandLine fromJavaParameters = CommandLineBuilder.createFromJavaParameters(javaParameters, project, false);
                            Process process = fromJavaParameters.createProcess();
                            myCheckInProgress.set(true);
                            StudyCheckTask checkTask = new StudyCheckTask(project, studyState, myCheckInProgress, process, fromJavaParameters.getCommandLineString());
                            ProgressManager.getInstance().run(checkTask);
                        } catch (ExecutionException e) {
                            LOG.error(e);
                        }
                    });

                });

            });
        });
    }

    @Nullable
    protected abstract VirtualFile getTestsFile(@NotNull final StudyState studyState);

    @Nullable
    private RunProfileState getState(RunnerAndConfigurationSettings javaTemplateConfiguration) {
        try {
            return javaTemplateConfiguration.getConfiguration().
                    getState(DefaultRunExecutor.getRunExecutorInstance(),
                            ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(),
                                    javaTemplateConfiguration).build());
        } catch (ExecutionException e) {
            LOG.error(e);
            return null;
        }
    }

    @NotNull
    private RunnerAndConfigurationSettings produceRunConfiguration(Project project, String name, ConfigurationType type) {
        return RunManager.getInstance(project).createRunConfiguration(name, type.getConfigurationFactories()[0]);
    }

    protected abstract void setProcessParameters(Project project, ApplicationConfiguration configuration,
                                                 VirtualFile taskFileVF, @NotNull VirtualFile testsFile);
}
