package org.stepik.core.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.stepik.core.StepikProjectManager;
import icons.AllStepikIcons;
import org.jetbrains.annotations.NotNull;

public class StudyToolWindowFactory implements ToolWindowFactory, DumbAware {
    public static final String STUDY_TOOL_WINDOW = "Step Description";
    private static final Logger logger = Logger.getInstance(StudyToolWindowFactory.class.getName());

    @Override
    public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        toolWindow.setIcon(AllStepikIcons.stepikLogoSmall);

        if (!StepikProjectManager.isStepikProject(project)) {
            logger.warn("Study Tool Window did not create");
            return;
        }

        final StudyToolWindow studyToolWindow = new StudyToolWindow();
        studyToolWindow.init(project);
        final ContentManager contentManager = toolWindow.getContentManager();
        final Content content = contentManager.getFactory().createContent(studyToolWindow, null, false);
        contentManager.addContent(content);
        Disposer.register(project, studyToolWindow);
        logger.info("Study Tool Window is created");
    }
}