package org.stepic.plugin.java.actions;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.jetbrains.edu.learning.courseFormat.Task;
import com.jetbrains.edu.learning.navigation.StudyNavigator;
import icons.InteractiveLearningIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StepicPreviousTaskAction extends StepicTaskNavigationAction {
    public static final String ACTION_ID = "PreviousTaskAction";
    public static final String SHORTCUT = "ctrl pressed COMMA";

    public StepicPreviousTaskAction() {
        super("Previous Task (" + KeymapUtil.getShortcutText(new KeyboardShortcut(KeyStroke.getKeyStroke(SHORTCUT), null)) + ")", "Navigate to the previous task", InteractiveLearningIcons.Prev);
    }

    @Override
    protected Task getTargetTask(@NotNull final Task sourceTask) {
        return StudyNavigator.previousTask(sourceTask);
    }

    @NotNull
    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Nullable
    @Override
    public String[] getShortcuts() {
        return new String[]{SHORTCUT};
    }
}