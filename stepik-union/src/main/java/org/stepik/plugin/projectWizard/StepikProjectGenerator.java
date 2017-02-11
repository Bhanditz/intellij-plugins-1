package org.stepik.plugin.projectWizard;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.jetbrains.tmp.learning.StepikProjectManager;
import com.jetbrains.tmp.learning.SupportedLanguages;
import com.jetbrains.tmp.learning.courseFormat.CourseNode;
import com.jetbrains.tmp.learning.stepik.StepikConnectorLogin;
import org.jetbrains.annotations.NotNull;
import org.stepik.api.client.StepikApiClient;
import org.stepik.api.exceptions.StepikClientException;
import org.stepik.api.objects.courses.Course;
import org.stepik.core.metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.stepik.core.metrics.MetricsStatus.DATA_NOT_LOADED;
import static org.stepik.core.metrics.MetricsStatus.SUCCESSFUL;
import static org.stepik.core.metrics.MetricsStatus.TARGET_NOT_FOUND;

public class StepikProjectGenerator {
    public static final Course EMPTY_COURSE = initEmptyCourse();
    private static final Logger logger = Logger.getInstance(StepikProjectGenerator.class);
    private static StepikProjectGenerator instance;
    private static CourseNode courseNode;
    private SupportedLanguages defaultLang = SupportedLanguages.INVALID;

    private StepikProjectGenerator() {
    }

    @NotNull
    private static Course initEmptyCourse() {
        Course course = new Course();
        course.setTitle("Empty");
        course.setDescription("Please, press refresh button");
        return course;
    }

    @NotNull
    public static StepikProjectGenerator getInstance() {
        if (instance == null) {
            instance = new StepikProjectGenerator();
        }
        return instance;
    }

    @NotNull
    private static List<Course> getCourses(@NotNull SupportedLanguages programmingLanguage) {
        List<Course> courses = new ArrayList<>();
        List<Long> coursesIds = getHardcodedCoursesId(programmingLanguage);

        if (!coursesIds.isEmpty()) {
            StepikApiClient stepikApiClient = StepikConnectorLogin.authAndGetStepikApiClient();
            try {
                courses = stepikApiClient.courses()
                        .get()
                        .id(coursesIds)
                        .execute()
                        .getCourses();
            } catch (StepikClientException e) {
                logger.warn("Failed get courses", e);
            }
        }

        courses.sort((course1, course2) -> {
            long id1 = course1.getId();
            long id2 = course2.getId();

            int index1 = coursesIds.indexOf(id1);
            int index2 = coursesIds.indexOf(id2);
            return Integer.compare(index1, index2);
        });

        return courses;
    }

    @NotNull
    private static List<Long> getHardcodedCoursesId(@NotNull SupportedLanguages programmingLanguage) {
        switch (programmingLanguage) {
            case JAVA:
            case JAVA8:
                return Arrays.asList(187L, 150L, 217L, 1127L, 125L, 126L);
            case PYTHON3:
                return Arrays.asList(67L, 512L, 401L, 217L, 1127L, 125L, 126L, 150L);
            case ASM32:
            case ASM64:
            case CLOJURE:
            case CPP:
            case CPP_11:
            case HASKELL:
            case HASKELL_7_10:
            case JAVASCRIPT:
            case MONO_CS:
            case OCTAVE:
            case R:
            case RUST:
            case SHELL:
                return Arrays.asList(217L, 1127L, 125L, 126L, 150L);
            case C:
            case HASKELL_8_0:
            case RUBY:
            case SCALA:
                return Arrays.asList(1127L, 125L, 126L, 150L);
        }

        return Collections.emptyList();
    }

    @NotNull
    public static List<Course> getCoursesUnderProgress(
            @NotNull final Project project,
            @NotNull SupportedLanguages programmingLanguage) {
        try {
            return ProgressManager.getInstance()
                    .runProcessWithProgressSynchronously(() -> {
                        ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
                        List<Course> courses = getCourses(programmingLanguage);
                        if (courses.isEmpty()) {
                            courses.add(EMPTY_COURSE);
                        }
                        return courses;
                    }, "Refreshing Course List", true, project);
        } catch (RuntimeException e) {
            return Collections.singletonList(EMPTY_COURSE);
        }
    }

    public void createCourseNodeUnderProgress(@NotNull final Project project, @NotNull Course course) {
        ProgressManager.getInstance()
                .runProcessWithProgressSynchronously(() -> {
                    if (course.getId() == 0) {
                        logger.warn("Failed to get a course");
                        Metrics.createProject(project, DATA_NOT_LOADED);
                        return;
                    }

                    ProgressIndicator indicator = ProgressManager.getInstance()
                            .getProgressIndicator();

                    courseNode = new CourseNode(course, indicator);
                }, "Creating project", true, project);
    }

    public void generateProject(@NotNull Project project) {
        StepikProjectManager stepikProjectManager = StepikProjectManager.getInstance(project);
        if (stepikProjectManager == null) {
            Metrics.createProject(project, TARGET_NOT_FOUND);
            return;
        }
        stepikProjectManager.setCourseNode(courseNode);
        stepikProjectManager.setDefaultLang(getDefaultLang());
        stepikProjectManager.setCreatedBy(StepikConnectorLogin.getCurrentUser().getId());
        Metrics.createProject(project, SUCCESSFUL);
    }

    @NotNull
    public SupportedLanguages getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(@NotNull SupportedLanguages defaultLang) {
        this.defaultLang = defaultLang;
    }
}
