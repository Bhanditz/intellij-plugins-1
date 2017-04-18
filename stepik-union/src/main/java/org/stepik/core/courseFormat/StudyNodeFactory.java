package org.stepik.core.courseFormat;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.stepik.api.client.StepikApiClient;
import org.stepik.api.objects.StudyObject;
import org.stepik.api.objects.courses.Course;
import org.stepik.api.objects.lessons.CompoundUnitLesson;
import org.stepik.api.objects.lessons.Lesson;
import org.stepik.api.objects.sections.Section;
import org.stepik.api.objects.steps.Step;

/**
 * @author meanmail
 */
public class StudyNodeFactory {
    public static StudyNode createTree(
            @NotNull Project project,
            @NotNull StepikApiClient stepikApiClient,
            @NotNull StudyObject data) {
        StudyNode root = null;
        if (data instanceof Course) {
            root = new CourseNode(project, stepikApiClient, (Course) data);
        } else if (data instanceof Section) {
            root = new SectionNode(project, stepikApiClient, (Section) data);
        } else if (data instanceof Lesson) {
            CompoundUnitLesson compoundData = new CompoundUnitLesson(null, (Lesson) data);
            root = new LessonNode(project, stepikApiClient, compoundData);
        } else if (data instanceof CompoundUnitLesson) {
            root = new LessonNode(project, stepikApiClient, (CompoundUnitLesson) data);
        } else if (data instanceof Step) {
            root = new StepNode(project, stepikApiClient, (Step) data);
        }
        return root;
    }
}