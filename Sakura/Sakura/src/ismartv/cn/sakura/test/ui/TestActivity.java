package ismartv.cn.sakura.test.ui;

import android.app.Activity;
import android.os.Bundle;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.ui.widget.progressbar.NumberProgressBar;
import cn.ismartv.speedtester.ui.widget.progressbar.SakuraProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fenghb on 14-7-24.
 */

public class TestActivity extends Activity {
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        init2();


    }

    private void init2() {

        final SakuraProgressBar sakuraProgressBar = (SakuraProgressBar) findViewById(R.id.sakura_progressbar);
        sakuraProgressBar.setProgress(80);
//        counter = 0;
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        sakuraProgressBar.setProgress(counter);
//                        counter += 1;
//                    }
//                });
//            }
//        }, 0, 5);


    }


    private void init1() {

        final NumberProgressBar bnp = (NumberProgressBar) findViewById(R.id.number_progressbar);
        counter = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(1);
                        counter++;
                        if (counter == 90) {
                            bnp.setProgress(90);
//                            counter = 0;

                        }
                    }
                });
            }
        }, 1000, 100);

    }


}
