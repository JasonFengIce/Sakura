package ismartv.cn.sakura.test.nosql;

import android.content.Context;
import colintmiller.com.simplenosql.NoSQL;
import colintmiller.com.simplenosql.OperationObserver;

import java.util.concurrent.CountDownLatch;

/**
 * Created by cmiller on 8/12/14.
 */
public class TestUtils {

    public static CountDownLatch cleanBucket(String bucket, Context context) {
        final CountDownLatch signal = new CountDownLatch(1);

        NoSQL.with(context, TestUtils.class)
                .bucketId(bucket)
                .addObserver(new OperationObserver() {
                    @Override
                    public void hasFinished() {
                        signal.countDown();
                    }
                })
                .delete();
        return signal;
    }
}
