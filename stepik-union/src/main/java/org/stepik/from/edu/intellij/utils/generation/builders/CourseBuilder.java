package org.stepik.from.edu.intellij.utils.generation.builders;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.jetbrains.tmp.learning.courseFormat.Course;
import org.stepik.from.edu.intellij.utils.generation.EduProjectGenerator;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface CourseBuilder {

    void createCourseFromGenerator(
            @NotNull ModifiableModuleModel moduleModel,
            @NotNull Project project,
            @NotNull EduProjectGenerator generator)
            throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException;

    void createLessonModules(
            @NotNull ModifiableModuleModel moduleModel,
            @NotNull Course course,
            @NotNull String moduleDir,
            @NotNull Project project)
            throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException;
}
