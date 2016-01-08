package tv.ismar.sakura.core;




import tv.ismar.sakura.data.http.ProblemEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaijie on 2/6/15.
 */
public class FeedbackProblem {

    private static tv.ismar.sakura.core.FeedbackProblem feedbackProblem;
    private List<ProblemEntity> mProblemEntities;

    public FeedbackProblem() {

    }

    public static tv.ismar.sakura.core.FeedbackProblem getInstance() {
        if (null == feedbackProblem)
            feedbackProblem = new tv.ismar.sakura.core.FeedbackProblem();
        return feedbackProblem;
    }

    public void saveCache(List<ProblemEntity> problemEntities) {
        this.mProblemEntities = problemEntities;
    }

    public List<ProblemEntity> getCache() {
        if (null == mProblemEntities) {
            mProblemEntities = new ArrayList<ProblemEntity>();
        }
        return mProblemEntities;
    }
}
