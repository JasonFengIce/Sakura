package app.tests;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.download.HttpDownloadTask;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by huaijie on 12/29/14.
 */
public class TestActivity extends Activity {
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

    }
}
