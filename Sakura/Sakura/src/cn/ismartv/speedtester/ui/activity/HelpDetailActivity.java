package cn.ismartv.speedtester.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import cn.ismartv.speedtester.R;

/**
 * Created by fenghb on 14-7-15.
 */
public class HelpDetailActivity extends Activity {
    private static final String TAG = "HelpDetailActivity";
    private static final int TOP_LEFT = 0;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_LEFT = 2;
    private static final int BOTTOM_RIGHT = 3;

    private TextView title;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_detail);
        title = (TextView)findViewById(R.id.title);
        textView1 = (TextView) findViewById(R.id.textview_1);
        textView2 = (TextView) findViewById(R.id.textview_2);
        textView3 = (TextView) findViewById(R.id.textview_3);
        textView4 = (TextView) findViewById(R.id.textview_4);
        textView5 = (TextView) findViewById(R.id.textview_5);

        SharedPreferences sharedPreferences = this.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        Log.d(TAG, intent.getIntExtra("position", -1) + "");
        switch (intent.getIntExtra("position", 1)) {
            case TOP_LEFT:
                title.setText(R.string.help1);
                textView1.setText(getResources().getString(R.string.hotline));
                textView2.setText(sharedPreferences.getString("vodcomm-hotline", ""));
                textView3.setText(getResources().getString(R.string.hotmail));
                textView4.setText(sharedPreferences.getString("vodcomm-hotmail", ""));
                break;
            case TOP_RIGHT:
                title.setText(R.string.help2);
                textView1.setText(getResources().getString(R.string.lenovohotline));
                textView2.setText(sharedPreferences.getString("apkproblem-lenovo", ""));
                textView3.setVisibility(View.INVISIBLE);
                textView4.setVisibility(View.INVISIBLE);
                textView5.setVisibility(View.INVISIBLE);
                break;
            case BOTTOM_LEFT:
                title.setText(R.string.help2);
                textView1.setText(getResources().getString(R.string.lenovohotline));
                textView2.setText(sharedPreferences.getString("apkproblem-lenovo", ""));
                textView3.setVisibility(View.INVISIBLE);
                textView4.setVisibility(View.INVISIBLE);
                break;
            case BOTTOM_RIGHT:
                title.setText(R.string.help1);
                textView1.setText(getResources().getString(R.string.hotline));
                textView2.setText(sharedPreferences.getString("vodcomm-hotline", ""));
                textView3.setText(getResources().getString(R.string.hotmail));
                textView4.setText(sharedPreferences.getString("vodcomm-hotmail", ""));
                break;
            default:
                break;
        }
    }


}
