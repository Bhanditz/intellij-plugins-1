package org.stepik.plugin.projectWizard.ui;

import com.jetbrains.tmp.learning.SupportedLanguages;
import org.jetbrains.annotations.NotNull;
import org.stepik.api.objects.StudyObject;

/**
 * @author meanmail
 */
interface ProjectSetting {
    void selectedStudyNode(@NotNull StudyObject course);

    void addListener(@NotNull ProjectSettingListener listener);

    void removeListener(@NotNull ProjectSettingListener listener);

    void selectedProgrammingLanguage(@NotNull SupportedLanguages language);
}