package org.stepic.plugin.java;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.jetbrains.edu.learning.StudyBasePluginConfigurator;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.courseFormat.Course;
import org.jetbrains.annotations.NotNull;
import org.stepic.plugin.java.actions.EduJavaCheckAction;

public class StepicJavaPluginConfigurator extends StudyBasePluginConfigurator {
    @NotNull
    @Override
    public DefaultActionGroup getActionGroup(Project project) {
        DefaultActionGroup baseGroup = super.getActionGroup(project);
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new EduJavaCheckAction());
        group.addAll(baseGroup);

        return group;
    }

    @NotNull
    @Override
    public String getDefaultHighlightingMode() {
        return "text/x-java";
    }

    @Override
    public boolean accept(@NotNull Project project) {
        StudyTaskManager instance = StudyTaskManager.getInstance(project);
        if (instance == null) return false;
        Course course = instance.getCourse();
//        return course != null && "PyCharm".equals(course.getCourseType()) && "JAVA".equals(course.getLanguage());
        return course != null;
    }

    @NotNull
    @Override
    public String getLanguageScriptUrl() {
        return getClass().getResource("/code_mirror/clike.js").toExternalForm();
    }
}
