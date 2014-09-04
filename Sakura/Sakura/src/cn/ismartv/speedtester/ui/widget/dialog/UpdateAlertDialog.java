package cn.ismartv.speedtester.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import cn.ismartv.speedtester.R;

/**
 * Created by huaijie on 8/8/14.
 */
public class UpdateAlertDialog extends Dialog {
    public UpdateAlertDialog(Context context) {
        super(context);
    }

    public UpdateAlertDialog(Context context, int theme) {
        super(context, theme);
    }

    protected UpdateAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_alert);
    }
}
