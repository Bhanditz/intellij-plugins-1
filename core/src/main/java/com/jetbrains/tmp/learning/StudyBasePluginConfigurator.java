package com.jetbrains.tmp.learning;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.tmp.learning.actions.*;
import com.jetbrains.tmp.learning.courseFormat.Task;
import com.jetbrains.tmp.learning.ui.StudyToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;

public abstract class StudyBasePluginConfigurator implements StudyPluginConfigurator {
    @NotNull
    @Override
    public DefaultActionGroup getActionGroup(Project project) {
        return getDefaultActionGroup();
    }

    @NotNull
    public static DefaultActionGroup getDefaultActionGroup() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new StudyRefreshTaskFileAction());
        group.add(new StudyShowHintAction());
        return group;
    }

    @NotNull
    @Override
    public Map<String, JPanel> getAdditionalPanels(Project project) {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public FileEditorManagerListener getFileEditorManagerListener(
            @NotNull Project project,
            @NotNull StudyToolWindow toolWindow) {

        return new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                Task task = StudyUtils.getTask(source.getProject(), file);
                setTaskText(task, file.getParent());
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                for (VirtualFile openedFile : source.getOpenFiles()) {
                    if (StudyUtils.getTask(project, openedFile) != null) {
                        return;
                    }
                }
                toolWindow.setEmptyText(project);
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                VirtualFile file = event.getNewFile();
                if (file != null) {
                    Task task = StudyUtils.getTask(event.getManager().getProject(), file);
                    setTaskText(task, file.getParent());
                }
                toolWindow.setBottomComponent(null);
            }

            private void setTaskText(@Nullable final Task task, @Nullable final VirtualFile taskDirectory) {
                String text = StudyUtils.getTaskTextFromTask(taskDirectory, task);
                if (text == null) {
                    toolWindow.setEmptyText(project);
                    return;
                }
                toolWindow.setTaskText(text, taskDirectory, project);
            }
        };
    }

    @Nullable
    @Override
    public StudyAfterCheckAction[] getAfterCheckActions() {
        return null;
    }
}
