package cn.ismartv.speedtester.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import cn.ismartv.speedtester.R;
import com.ismartv.android.vod.service.HttpProxyService;

import java.util.List;

/**
 * Created by huaijie on 14-11-12.
 */
public class MenuActivity extends BaseActivity {
    public static final String TAB_FLAG = "TAB_FLAG";
    public static final int TAB_SPEED = 0;
    public static final int TAB_HELP = 1;
    public static final int TAB_FEEDBACK = 2;

//    private ShowcaseView showcaseView;

    @InjectViews({R.id.tab_speed, R.id.tab_help, R.id.tab_feedback})
    List<ImageView> tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent ootStartIntent = new Intent(this, HttpProxyService.class);
        this.startService(ootStartIntent);
        setContentView(R.layout.activity_menu);
        ButterKnife.inject(this);
        ///////////////////////////////////////////////////////////////
        //Add Showcase View
        ///////////////////////////////////////////////////////////////
//        showcaseView = new ShowcaseView.Builder(this)
//                .setTarget(new ViewTarget(findViewById(R.id.tab_speed)))
//                .setOnClickListener(this)
//                .build();
//        showcaseView.setButtonText(getString(R.string.next));
    }


    @OnClick({R.id.tab_speed, R.id.tab_help, R.id.tab_feedback})
    public void pickTab(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        switch (view.getId()) {
            case R.id.tab_speed:
                intent.putExtra(TAB_FLAG, TAB_SPEED);
                break;
            case R.id.tab_help:
                intent.putExtra(TAB_FLAG, TAB_HELP);
                break;
            case R.id.tab_feedback:
                intent.putExtra(TAB_FLAG, TAB_FEEDBACK);
                break;
            default:
                break;
        }
        startActivity(intent);
    }

}
