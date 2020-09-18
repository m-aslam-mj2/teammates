package teammates.test.cases.webapi;

import java.util.List;

import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.util.Const;
import teammates.common.util.Const.ParamsNames;
import teammates.common.util.EmailType;
import teammates.common.util.EmailWrapper;
import teammates.common.util.TaskWrapper;
import teammates.logic.core.CoursesLogic;
import teammates.ui.request.SendEmailRequest;
import teammates.ui.webapi.FeedbackSessionPublishedEmailWorkerAction;

/**
 * SUT: {@link FeedbackSessionPublishedEmailWorkerAction}.
 */
public class FeedbackSessionPublishedEmailWorkerActionTest
        extends BaseActionTest<FeedbackSessionPublishedEmailWorkerAction> {

    private static final CoursesLogic coursesLogic = CoursesLogic.inst();

    @Override
    protected String getActionUri() {
        return Const.TaskQueue.FEEDBACK_SESSION_PUBLISHED_EMAIL_WORKER_URL;
    }

    @Override
    protected String getRequestMethod() {
        return POST;
    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        verifyOnlyAdminCanAccess();
    }

    @Override
    @Test
    public void testExecute() {
        FeedbackSessionAttributes session1 = typicalBundle.feedbackSessions.get("session1InCourse1");

        String[] submissionParams = new String[] {
                ParamsNames.COURSE_ID, session1.getCourseId(),
                ParamsNames.FEEDBACK_SESSION_NAME, session1.getFeedbackSessionName(),
        };

        FeedbackSessionPublishedEmailWorkerAction action = getAction(submissionParams);
        action.execute();

        // 5 students and 5 instructors in course1
        verifySpecifiedTasksAdded(action, Const.TaskQueue.SEND_EMAIL_QUEUE_NAME, 10);

        String courseName = coursesLogic.getCourse(session1.getCourseId()).getName();
        List<TaskWrapper> tasksAdded = action.getTaskQueuer().getTasksAdded();
        for (TaskWrapper task : tasksAdded) {
            SendEmailRequest requestBody = (SendEmailRequest) task.getRequestBody();
            EmailWrapper email = requestBody.getEmail();
            assertEquals(String.format(EmailType.FEEDBACK_PUBLISHED.getSubject(), courseName,
                                       session1.getFeedbackSessionName()),
                         email.getSubject());
        }
    }

}
