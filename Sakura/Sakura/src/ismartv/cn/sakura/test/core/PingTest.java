package ismartv.cn.sakura.test.core;

import android.test.AndroidTestCase;
import android.util.Log;
import cn.ismartv.speedtester.core.ping.Ping;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by fenghb on 14-7-4.
 */
public class PingTest extends AndroidTestCase {
    private static final String TAG = "PingTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        download();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void ping() {
        Ping ping = new Ping();
//        Log.d(TAG, String.valueOf(ping.ping()));
    }

    public void download() {
        String HOST = "http://192.168.16.103:8099";
        String GET_NODE_URL = "/shipinkefu/getCdninfo";

        HttpClient client = new DefaultHttpClient();

        HttpGet get = new HttpGet(HOST + GET_NODE_URL);
        HttpResponse response = null;
        try {
            response = client.execute(get);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        if (response.getStatusLine().
                getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();
            String result = new String();
            try {
                result = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, result);
        } else {
            Log.e(TAG, response.getStatusLine().toString());
        }
    }
}
