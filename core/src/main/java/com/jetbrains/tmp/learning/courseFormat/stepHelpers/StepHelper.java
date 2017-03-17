package com.jetbrains.tmp.learning.courseFormat.stepHelpers;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.tmp.learning.courseFormat.StepNode;
import com.jetbrains.tmp.learning.courseFormat.StudyStatus;
import com.jetbrains.tmp.learning.stepik.StepikConnectorLogin;
import org.jetbrains.annotations.NotNull;
import org.stepik.api.client.StepikApiClient;
import org.stepik.api.exceptions.StepikClientException;
import org.stepik.api.objects.attempts.Attempt;
import org.stepik.api.objects.attempts.Attempts;
import org.stepik.api.objects.attempts.Component;
import org.stepik.api.objects.attempts.Dataset;
import org.stepik.api.objects.submissions.Reply;
import org.stepik.api.objects.submissions.Submission;
import org.stepik.api.objects.submissions.Submissions;
import org.stepik.api.objects.users.User;
import org.stepik.api.queries.Order;
import org.stepik.api.urls.Urls;

import java.util.List;

/**
 * @author meanmail
 */
public class StepHelper {
    private static final Logger logger = Logger.getInstance(StepHelper.class);
    private final StepNode stepNode;
    @NotNull
    Reply reply = new Reply();
    @NotNull
    private String status = "";
    @NotNull
    private Attempt attempt = new Attempt();
    private int submissionsCount = -1;
    private Submission submission;
    private boolean inited;

    public StepHelper(@NotNull StepNode stepNode) {
        this.stepNode = stepNode;
    }

    @NotNull
    public StepNode getStepNode() {
        return stepNode;
    }

    private boolean loadAttempt(@NotNull StepikApiClient stepikApiClient, long userId) {
        Attempts attempts = stepikApiClient.attempts()
                .get()
                .step(stepNode.getId())
                .user(userId)
                .execute();
        if (attempts.isEmpty()) {
            attempt = new Attempt();
            return false;
        }

        attempt = attempts.getAttempts().get(0);
        return true;
    }

    private boolean loadSubmission(StepikApiClient stepikApiClient, long userId) {
        Submissions submissions = stepikApiClient.submissions()
                .get()
                .order(Order.DESC)
                .attempt(attempt.getId())
                .user(userId)
                .execute();

        if (!submissions.isEmpty()) {
            submission = submissions.getSubmissions().get(0);
            reply = submission.getReply();
            status = submission.getStatus();
            stepNode.setStatus(StudyStatus.of(status));
            return true;
        }

        status = "active";
        return false;
    }

    @NotNull
    public String getStatus() {
        initStepOptions();
        return status;
    }

    @NotNull
    public String getPath() {
        return stepNode.getPath();
    }

    public long getAttemptId() {
        initStepOptions();
        return attempt.getId();
    }

    @NotNull
    Dataset getDataset() {
        initStepOptions();
        return attempt.getDataset();
    }

    @NotNull
    public String getDatasetUrl() {
        initStepOptions();
        return attempt.getDatasetUrl();
    }

    public int getTimeLeft() {
        initStepOptions();
        return attempt.getTimeLeft();
    }

    @NotNull
    public String getReplyUrl() {
        initStepOptions();
        return submission != null ? submission.getReplyUrl() : "";
    }

    public String getBaseUrl() {
        return Urls.STEPIK_URL;
    }

    void initStepOptions() {
        if (!needInit()) {
            return;
        }

        onStartInit();
        status = "";

        try {
            StepikApiClient stepikApiClient = StepikConnectorLogin.authAndGetStepikApiClient();
            User user = StepikConnectorLogin.getCurrentUser();
            long userId = user.getId();

            if (!loadAttempt(stepikApiClient, userId)) {
                return;
            }

            onAttemptLoaded();

            if (loadSubmission(stepikApiClient, userId)) {
                onSubmissionLoaded();
            }

            onFinishInit();
        } catch (StepikClientException e) {
            logger.warn("Failed init test-step options", e);
            onInitFailed();
        }
    }

    boolean needInit() {
        return !inited;
    }

    void onStartInit() {
    }

    void onAttemptLoaded() {
    }

    void onSubmissionLoaded() {
    }

    void onFinishInit() {
        inited = true;
    }

    void onInitFailed() {
        inited = false;
    }

    public int getSubmissionsCount() {
        if (submissionsCount == -1) {
            try {
                StepikApiClient stepikApiClient = StepikConnectorLogin.authAndGetStepikApiClient();
                User user = StepikConnectorLogin.getCurrentUser();
                long userId = user.getId();
                submissionsCount = 0;
                Submissions submissions;
                int page = 1;
                do {
                    submissions = stepikApiClient.submissions()
                            .get()
                            .step(stepNode.getId())
                            .user(userId)
                            .page(page)
                            .execute();
                    submissionsCount += submissions.getCount();
                    page++;
                } while (submissions.getMeta().getHasNext());
            } catch (StepikClientException e) {
                logger.warn("Failed get submissions count", e);
                return 0;
            }
        }
        return submissionsCount;
    }

    @NotNull
    public String getType() {
        return stepNode.getType().getName();
    }

    @NotNull
    public List<Component> getComponents() {
        return getDataset().getComponents();
    }

    public List<String> getBlanks() {
        initStepOptions();
        return reply.getBlanks();
    }
}
