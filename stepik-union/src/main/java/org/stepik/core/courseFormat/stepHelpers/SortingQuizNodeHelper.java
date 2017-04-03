package org.stepik.core.courseFormat.stepHelpers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.stepik.core.courseFormat.StepNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author meanmail
 */
public class SortingQuizNodeHelper extends QuizHelper {
    private String[] values;
    private List<Integer> replyOrdering;
    private List<Pair<Integer, String>> ordering;

    public SortingQuizNodeHelper(@NotNull Project project, @NotNull StepNode stepNode) {
        super(project, stepNode);
    }

    @NotNull
    public List<Pair<Integer, String>> getOrdering() {
        initStepOptions();
        return ordering;
    }

    @Override
    protected boolean needInit() {
        return ordering == null;
    }

    @Override
    protected void onStartInit() {
        ordering = new ArrayList<>();
    }

    @Override
    protected void onAttemptLoaded() {
        values = getDataset().getOptions();
    }

    @Override
    protected void onSubmissionLoaded() {
        replyOrdering = reply.getOrdering();
    }

    @Override
    protected void onFinishInit() {
        if (replyOrdering == null) {
            replyOrdering = new ArrayList<>();
            for (int i = 0; i < values.length; i++)
                replyOrdering.add(i);
        }

        for (int index : replyOrdering) {
            ordering.add(Pair.create(index, index < values.length ? values[index] : ""));
        }
    }

    @Override
    void onInitFailed() {
        ordering = null;
    }
}
