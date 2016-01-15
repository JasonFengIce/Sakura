package tv.ismar.sakura.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

/**
 * Created by huaijie on 1/15/16.
 */
public class BaseFragment extends Fragment {
    private float densityRate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDensityRate();
    }

    public float getDensityRate() {
        return densityRate;
    }

    private void setDensityRate() {
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        this.densityRate = (float) densityDpi / (float) 160;
    }
}
