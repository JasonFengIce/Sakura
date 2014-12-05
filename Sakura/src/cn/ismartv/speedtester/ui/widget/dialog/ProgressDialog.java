package cn.ismartv.speedtester.ui.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by huaijie on 12/5/14.
 */
public class ProgressDialog extends DialogFragment {


    private static final String TAG = "ProgressDialog";

    private Activity mActivity;

    public static ProgressDialog newInstance() {
        return new ProgressDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
