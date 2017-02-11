package com.jetbrains.tmp.learning.courseFormat;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.stepik.StepikConnectorLogin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepik.api.client.StepikApiClient;
import org.stepik.api.exceptions.StepikClientException;
import org.stepik.api.objects.lessons.CompoundUnitLesson;
import org.stepik.api.objects.sections.Sections;
import org.stepik.api.objects.steps.Step;
import org.stepik.api.objects.steps.Steps;

import java.util.List;
import java.util.stream.Collectors;

import static com.jetbrains.tmp.learning.stepik.StepikConnectorLogin.authAndGetStepikApiClient;

public class LessonNode extends Node<CompoundUnitLesson, StepNode, Step, StepNode> {
    private static final Logger logger = Logger.getInstance(LessonNode.class);
    private long courseId;

    public LessonNode() {
    }

    public LessonNode(@NotNull final SectionNode parent, @NotNull CompoundUnitLesson data) {
        super(parent, data);
    }


    @Override
    protected List<Step> getChildDataList() {
        Steps steps = new Steps();
        try {
            StepikApiClient stepikApiClient = StepikConnectorLogin.getStepikApiClient();

            List<Long> stepsIds = getData().getLesson().getSteps();

            if (stepsIds.size() > 0) {
                steps = stepikApiClient.steps()
                        .get()
                        .id(stepsIds)
                        .execute();


            }
        } catch (StepikClientException | IllegalAccessException | InstantiationException logged) {
            logger.warn("A lesson initialization don't is fully", logged);
        }

        return steps.getSteps().stream()
                .filter(step -> StepType.of(step.getBlock().getName()) == StepType.CODE)
                .collect(Collectors.toList());
    }

    @Override
    public void init(@Nullable StudyNode parent, boolean isRestarted, @Nullable ProgressIndicator indicator) {
        if (indicator != null) {
            indicator.setText("Refresh a lesson: " + getName());
            indicator.setText2("Update steps");
        }

        courseId = 0;

        super.init(parent, isRestarted, indicator);
    }


    @Override
    protected Class<StepNode> getChildClass() {
        return StepNode.class;
    }

    @NotNull
    @Override
    public String getName() {
        try {
            return getData().getLesson().getTitle();
        } catch (IllegalAccessException | InstantiationException e) {
            return "";
        }
    }

    @Override
    public long getCourseId() {
        StudyNode parent = getParent();
        if (parent != null) {
            return parent.getCourseId();
        }

        if (courseId != 0) {
            return courseId;
        }

        int sectionId;
        try {
            sectionId = getData().getUnit().getSection();
        } catch (IllegalAccessException | InstantiationException e) {
            return 0;
        }
        if (sectionId == 0) {
            return 0;
        }

        try {
            StepikApiClient stepikApiClient = authAndGetStepikApiClient();

            Sections sections = stepikApiClient.sections()
                    .get()
                    .id(sectionId)
                    .execute();
            if (sections.isEmpty()) {
                return 0;
            }
            courseId = sections.getItems().get(0).getCourse();
            return courseId;
        } catch (StepikClientException ignored) {
        }
        return 0;
    }

    public int getPosition() {
        try {
            return getData().getUnit().getPosition();
        } catch (IllegalAccessException | InstantiationException e) {
            return 0;
        }
    }

    @Override
    protected Class<CompoundUnitLesson> getDataClass() {
        return CompoundUnitLesson.class;
    }

    @NotNull
    @Override
    String getDirectoryPrefix() {
        return EduNames.LESSON;
    }
}
