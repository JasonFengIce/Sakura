package cn.ismartv.speedtester.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import cn.ismartv.speedtester.R;

/**
 * Created by fenghb on 14-7-15.
 */
public class HelpFragment extends Fragment {
    private static final String URL = "http://192.168.1.185:8099/shipinkefu/help.html";
    private WebView helpPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help, null);
        view.setBackgroundColor(getResources().getColor(
                R.color.help_fragment_bg));
        helpPage = (WebView) view.findViewById(R.id.help_page);

        helpPage.getSettings().setJavaScriptEnabled(true);

        helpPage.setBackgroundColor(getResources().getColor(
                R.color.help_fragment_bg));
        helpPage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        helpPage.requestFocus(View.FOCUS_DOWN | View.FOCUS_UP);
        helpPage.getSettings().setLightTouchEnabled(true);
        helpPage.setOnKeyListener(keyeventListener);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        helpPage.loadUrl(URL);

    }

    private int index = -1;

    public void setKeyEvent(int keyCode) {
        if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
            index--;
            if (index < 0)
                index = 0;
        }
        if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
            index++;
            if (index > 2)
                index = 2;
        }
        if (index == 0) {
            helpPage.loadUrl("javascript:setTab(0)");
        } else if (index == 1) {
            helpPage.loadUrl("javascript:setTab(1)");
        } else if (index == 2) {
            helpPage.loadUrl("javascript:setTab(2)");
        }
    }

    View.OnKeyListener keyeventListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
                setKeyEvent(keyCode);
            return false;
        }
    };
}
