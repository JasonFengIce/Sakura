package tv.ismar.sakura.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.content.ContentProvider;
import cn.ismartv.injectdb.library.query.Select;
import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.sakura.R;
import tv.ismar.sakura.core.client.OkHttpClientManager;
import tv.ismar.sakura.data.http.BindedCdnEntity;

import static tv.ismar.sakura.core.SakuraClientAPI.BindCdn;
import static tv.ismar.sakura.core.SakuraClientAPI.DeviceLog;
import static tv.ismar.sakura.core.SakuraClientAPI.GetBindCdn;
import static tv.ismar.sakura.core.SakuraClientAPI.UnbindNode;
import static tv.ismar.sakura.core.SakuraClientAPI.UploadResult;

/**
 * Created by huaijie on 2015/4/8.
 */
public class NodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        tv.ismar.sakura.core.HttpDownloadTask.OnCompleteListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "NodeFragment";

    private static String NORMAL_SELECTION = tv.ismar.sakura.data.table.CdnTable.DISTRICT_ID + "=? and " + tv.ismar.sakura.data.table.CdnTable.ISP_ID + "=?" + " or " + tv.ismar.sakura.data.table.CdnTable.CDN_FLAG + "  <> ?" + " ORDER BY " + tv.ismar.sakura.data.table.CdnTable.ISP_ID + " DESC," + tv.ismar.sakura.data.table.CdnTable.SPEED + " DESC";
    private static String OTHER_SELECTION = tv.ismar.sakura.data.table.CdnTable.DISTRICT_ID + "=? and " + tv.ismar.sakura.data.table.CdnTable.ISP_ID + " in (?, ?)" + " or " + tv.ismar.sakura.data.table.CdnTable.CDN_FLAG + "  <> ?" + " ORDER BY " + tv.ismar.sakura.data.table.CdnTable.ISP_ID + " DESC," + tv.ismar.sakura.data.table.CdnTable.SPEED + " DESC";

    private static final String NOT_THIRD_CDN = "0";

    private static final int NORMAL_ISP_FLAG = 01245;
    private static final int OTHER_ISP_FLAG = 3;

    private String TIE_TONG = "";

    private tv.ismar.sakura.ui.widget.SakuraListView nodeListView;
    private tv.ismar.sakura.ui.adapter.NodeListAdapter nodeListAdapter;
    private TextView currentNodeTextView;
    private tv.ismar.sakura.ui.widget.SakuraButton unbindButton;
    private Spinner provinceSpinner;
    private Spinner ispSpinner;
    private tv.ismar.sakura.ui.widget.SakuraButton speedTestButton;

    private tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment selectNodePup;
    private Dialog cdnTestDialog;
    private tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment cdnTestCompletedPop;


    private String[] selectionArgs = {"0", "0"};
    private String[] cities;

    private String mDistrictId = "";
    private String mIspId = "";

    /**
     * 传入下载中的 CDN 节点 ID
     */
    private List<Integer> cdnCollections;

    private String snCode = TextUtils.isEmpty(tv.ismar.sakura.core.SimpleRestClient.sn_token) ? "sn is null" : tv.ismar.sakura.core.SimpleRestClient.sn_token;


    private Context mContext;

    private tv.ismar.sakura.core.HttpDownloadTask httpDownloadTask;


    private float rate;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TIE_TONG = tv.ismar.sakura.utils.StringUtils.getMd5Code("铁通");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_node, null);
        currentNodeTextView = (TextView) view.findViewById(R.id.current_node_text);
        unbindButton = (tv.ismar.sakura.ui.widget.SakuraButton) view.findViewById(R.id.unbind_node);
        nodeListView = (tv.ismar.sakura.ui.widget.SakuraListView) view.findViewById(R.id.node_list);
        nodeListAdapter = new tv.ismar.sakura.ui.adapter.NodeListAdapter(mContext, null, true);
        nodeListView.setAdapter(nodeListAdapter);

        provinceSpinner = (Spinner) view.findViewById(R.id.province_spinner);
        ispSpinner = (Spinner) view.findViewById(R.id.isp_spinner);

        speedTestButton = (tv.ismar.sakura.ui.widget.SakuraButton) view.findViewById(R.id.speed_test_btn);

        speedTestButton.setOnClickListener(this);
        nodeListView.setOnItemClickListener(this);
        unbindButton.setOnClickListener(this);

        nodeListView.setNextFocusDownId(nodeListView.getId());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        List<tv.ismar.sakura.data.table.ProvinceTable> provinceTables = new Select().from(tv.ismar.sakura.data.table.ProvinceTable.class).execute();
        tv.ismar.sakura.ui.adapter.ProvinceSpinnerAdapter provinceSpinnerAdapter = new tv.ismar.sakura.ui.adapter.ProvinceSpinnerAdapter(mContext, provinceTables);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);
        String accountProvince = tv.ismar.sakura.core.preferences.AccountSharedPrefs.getInstance(mContext).getSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.PROVINCE);

        tv.ismar.sakura.data.table.ProvinceTable provinceTable = new Select().from(tv.ismar.sakura.data.table.ProvinceTable.class).
                where(tv.ismar.sakura.data.table.ProvinceTable.PROVINCE_NAME + " = ?", accountProvince).executeSingle();
        if (provinceTable != null) {
            provinceSpinner.setSelection((provinceTable.getId().intValue() - 1));
        }


        List<tv.ismar.sakura.data.table.IspTable> ispTables = new Select().from(tv.ismar.sakura.data.table.IspTable.class).execute();
        tv.ismar.sakura.ui.adapter.IspSpinnerAdapter ispSpinnerAdapter = new tv.ismar.sakura.ui.adapter.IspSpinnerAdapter(mContext, ispTables);
        ispSpinner.setAdapter(ispSpinnerAdapter);

        String accountIsp = tv.ismar.sakura.core.preferences.AccountSharedPrefs.getInstance(mContext).getSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.ISP);
        tv.ismar.sakura.data.table.IspTable ispTable = new Select().from(tv.ismar.sakura.data.table.IspTable.class).where(tv.ismar.sakura.data.table.IspTable.ISP_NAME + " = ?", accountIsp).executeSingle();
        if (ispTable != null) {
            ispSpinner.setSelection(ispTable.getId().intValue() - 1);
        }
        setSpinnerItemSelectedListener();
        getLoaderManager().initLoader(NORMAL_ISP_FLAG, null, this);

        speedTestButton.requestFocus();


    }

    @Override
    public Loader onCreateLoader(int flag, Bundle args) {
        CursorLoader cacheLoader = new tv.ismar.sakura.core.CdnCacheLoader(mContext, ContentProvider.createUri(tv.ismar.sakura.data.table.CdnTable.class, null),
                null, null, null, null);
        switch (flag) {
            case NORMAL_ISP_FLAG:
                cacheLoader.setSelection(NORMAL_SELECTION);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            case OTHER_ISP_FLAG:
                cacheLoader.setSelection(OTHER_SELECTION);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            default:
                break;
        }
        return cacheLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cdnCollections = cursorToList(data);
        nodeListAdapter.swapCursor(data);
        updateCurrentNode();
    }


    @Override
    public void onLoaderReset(Loader loader) {
        nodeListAdapter.swapCursor(null);
    }

    private void setSpinnerItemSelectedListener() {
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tv.ismar.sakura.data.table.ProvinceTable provinceTable = new Select().from(tv.ismar.sakura.data.table.ProvinceTable.class).where("_id = ?", position + 1).executeSingle();
                if (provinceTable != null) {
                    mDistrictId = provinceTable.district_id;
                    notifiySourceChanged();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ispSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tv.ismar.sakura.data.table.IspTable ispTable = new Select().from(tv.ismar.sakura.data.table.IspTable.class).where("_id = ?", position + 1).executeSingle();
                if (ispTable != null) {
                    mIspId = ispTable.isp_id;
                    notifiySourceChanged();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void notifiySourceChanged() {
        if (mIspId.equals(TIE_TONG)) {

            String unicom = tv.ismar.sakura.utils.StringUtils.getMd5Code("联通");
            String chinaMobile = tv.ismar.sakura.utils.StringUtils.getMd5Code("移动");
            selectionArgs = new String[]{mDistrictId, chinaMobile, unicom, NOT_THIRD_CDN};
            getLoaderManager().destroyLoader(NORMAL_ISP_FLAG);
            getLoaderManager().restartLoader(OTHER_ISP_FLAG, null, NodeFragment.this).forceLoad();
        } else {
            selectionArgs = new String[]{mDistrictId, mIspId, NOT_THIRD_CDN};
            getLoaderManager().destroyLoader(OTHER_ISP_FLAG);
            getLoaderManager().restartLoader(NORMAL_ISP_FLAG, null, NodeFragment.this).forceLoad();
        }

    }

    @Override
    public void onDestroy() {
        if (null != selectNodePup) {
            selectNodePup.dismiss();
        }
        if (null != cdnTestCompletedPop) {
            cdnTestCompletedPop.dismiss();
        }
        if (null != cdnTestDialog) {
            cdnTestDialog.dismiss();
        }

        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unbind_node:
                unbindNode(tv.ismar.sakura.core.SimpleRestClient.sn_token);
                break;
            case R.id.speed_test_btn:
                showCdnTestDialog();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        showSelectNodePop((Integer) view.getTag());
    }

    //    /**
//     * fetchBindedCdn
//     *
//     * @param snCode
//     */
    private void fetchBindedCdn(final String snCode) {
        GetBindCdn client = OkHttpClientManager.getInstance().restAdapter_WX_API_TVXIO.create(GetBindCdn.class);
        client.excute(snCode).enqueue(new Callback<BindedCdnEntity>() {
            @Override
            public void onResponse(Response<BindedCdnEntity> response) {
                BindedCdnEntity bindedCdnEntity = response.body();
                if (BindedCdnEntity.NO_RECORD.equals(bindedCdnEntity.getRetcode())) {
                    clearCheck();
                } else {
                    updateCheck(Integer.parseInt(bindedCdnEntity.getSncdn().getCdnid()));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "fetchBindedCdn error!!!");
            }
        });

    }

    private void bindCdn(final String snCode, final int cdnId) {
        BindCdn client = OkHttpClientManager.getInstance().restAdapter_WX_API_TVXIO.create(BindCdn.class);
        client.excute(snCode, cdnId).enqueue(new Callback<tv.ismar.sakura.data.http.Empty>() {

            @Override
            public void onResponse(Response<tv.ismar.sakura.data.http.Empty> response) {
                Toast.makeText(mContext, R.string.node_bind_success, Toast.LENGTH_LONG).show();
                fetchBindedCdn(snCode);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "bindCdn error");
            }
        });


    }

    private void unbindNode(final String snCode) {
        UnbindNode client = OkHttpClientManager.getInstance().restAdapter_WX_API_TVXIO.create(UnbindNode.class);
        client.excute(snCode).enqueue(new Callback<tv.ismar.sakura.data.http.Empty>() {
            @Override
            public void onResponse(Response<tv.ismar.sakura.data.http.Empty> response) {
                fetchBindedCdn(snCode);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "unbindNode");
            }
        });
    }

    /**
     * updateCheck
     *
     * @param cdnId
     */
    public static void updateCheck(int cdnId) {
        ActiveAndroid.beginTransaction();

        try {
            tv.ismar.sakura.data.table.CdnTable checkedItem = new Select().from(tv.ismar.sakura.data.table.CdnTable.class).where(tv.ismar.sakura.data.table.CdnTable.CHECKED + " = ?", true).executeSingle();
            if (null != checkedItem) {
                checkedItem.checked = false;
                checkedItem.save();
            }

            tv.ismar.sakura.data.table.CdnTable cdnCacheTable = new Select().from(tv.ismar.sakura.data.table.CdnTable.class).where(tv.ismar.sakura.data.table.CdnTable.CDN_ID + " = ?", cdnId).executeSingle();
            if (null != cdnCacheTable) {
                cdnCacheTable.checked = true;
                cdnCacheTable.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * clearCheck
     */
    private void clearCheck() {
        tv.ismar.sakura.data.table.CdnTable checkedItem = new Select().from(tv.ismar.sakura.data.table.CdnTable.class).where("checked = ?", true).executeSingle();
        if (null != checkedItem) {
            checkedItem.checked = false;
            checkedItem.save();
        }
    }

    private void updateCurrentNode() {
        tv.ismar.sakura.data.table.CdnTable cdnCacheTable = new Select().from(tv.ismar.sakura.data.table.CdnTable.class).where("checked = ?", true).executeSingle();
        if (cdnCacheTable != null) {
            currentNodeTextView.setText(getText(R.string.current_node) + cdnCacheTable.cdn_nick);
            unbindButton.setText(R.string.switch_to_auto);
            unbindButton.setEnabled(true);
            unbindButton.setFocusable(true);
            unbindButton.setFocusableInTouchMode(true);
        } else {
            currentNodeTextView.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
            unbindButton.setText(R.string.already_to_auto);
            unbindButton.setEnabled(false);
            unbindButton.setFocusable(false);
            unbindButton.setFocusableInTouchMode(false);
        }

    }


    /**
     * showSelectNodePop
     *
     * @param cndId
     */
    private void showSelectNodePop(final int cndId) {
        selectNodePup = new tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment(mContext, getString(R.string.are_you_sure_selecte), null);
        selectNodePup.showAtLocation(getView(), Gravity.CENTER, new tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment.ConfirmListener() {
            @Override
            public void confirmClick(View view) {
                bindCdn(tv.ismar.sakura.core.SimpleRestClient.sn_token, cndId);
                selectNodePup.dismiss();
            }
        }, new tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment.CancelListener() {
            @Override
            public void cancelClick(View view) {
                selectNodePup.dismiss();
            }
        });
    }


    private void showCdnTestDialog() {
        cdnTestDialog = new Dialog(mContext, R.style.ProgressDialog);
        Window dialogWindow = cdnTestDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.width = 400;
        lp.height = 150;
        View mView = LayoutInflater.from(mContext).inflate(R.layout.sakura_dialog_cdn_test_progress, null);
        cdnTestDialog.setContentView(mView, lp);
        cdnTestDialog.setCanceledOnTouchOutside(false);

        cdnTestDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_ESCAPE:
                        dialog.dismiss();
                        showCdnTestCompletedPop(Status.CANCEL);
                        return true;
                }
                return false;
            }
        });

        cdnTestDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                /**
                 * 开始测速
                 */
                httpDownloadTask = new tv.ismar.sakura.core.HttpDownloadTask(mContext);
                httpDownloadTask.setCompleteListener(NodeFragment.this);
                httpDownloadTask.execute(cdnCollections);
            }
        });
        cdnTestDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                httpDownloadTask.cancel(true);
                httpDownloadTask = null;
            }
        });
        cdnTestDialog.show();
    }


    private void showCdnTestCompletedPop(final Status status) {
        int titleRes;
        speedTestButton.clearFocus();
        switch (status) {
            case COMPLETE:

                titleRes = R.string.test_complete_text;
                break;
            case CANCEL:
                titleRes = R.string.test_interupt;
                break;
            default:
                titleRes = R.string.test_complete_text;
                break;
        }


        cdnTestCompletedPop = new tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment(mContext, getString(titleRes), null);
        cdnTestCompletedPop.showAtLocation(getView(), Gravity.CENTER, new tv.ismar.sakura.ui.widget.dialog.MessageDialogFragment.ConfirmListener() {
            @Override
            public void confirmClick(View view) {
                cdnTestCompletedPop.dismiss();
                nodeListView.setSelectionOne();
            }
        }, null);

    }

    @Override
    public void onSingleComplete(String cndId, String nodeName, String speed) {
        tv.ismar.sakura.data.http.SpeedLogEntity speedLog = new tv.ismar.sakura.data.http.SpeedLogEntity();
        speedLog.setCdn_id(cndId);
        speedLog.setCdn_name(nodeName);
        speedLog.setSpeed(speed);

        speedLog.setLocation(tv.ismar.sakura.core.preferences.AccountSharedPrefs.getInstance(mContext).getSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.CITY));
        speedLog.setLocation(tv.ismar.sakura.core.preferences.AccountSharedPrefs.getInstance(mContext).getSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.ISP));


        Gson gson = new Gson();
        String data = gson.toJson(speedLog, tv.ismar.sakura.data.http.SpeedLogEntity.class);
        String base64Data = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);


        uploadTestResult(cndId, speed);
        uploadCdnTestLog(base64Data, snCode, Build.MODEL);
    }

    @Override
    public void onAllComplete() {
        if (cdnTestDialog != null) {
            cdnTestDialog.dismiss();
        }
        showCdnTestCompletedPop(Status.COMPLETE);

    }

    @Override
    public void onCancel() {

    }


    private void uploadCdnTestLog(String data, String snCode, String model) {
        DeviceLog client = OkHttpClientManager.getInstance().restAdapter_SPEED_CALLA_TVXIO.create(DeviceLog.class);
        client.execute(data, snCode, model).enqueue(new Callback<tv.ismar.sakura.data.http.Empty>() {
            @Override
            public void onResponse(Response<tv.ismar.sakura.data.http.Empty> response) {
                Log.d(TAG, "uploadCdnTestLog success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "uploadCdnTestLog");
            }
        });


    }


    public void uploadTestResult(String cdnId, String speed) {
        UploadResult client = OkHttpClientManager.getInstance().restAdapter_WX_API_TVXIO.create(UploadResult.class);
        client.excute(UploadResult.ACTION_TYPE, snCode, cdnId, speed).enqueue(new Callback<tv.ismar.sakura.data.http.Empty>() {
            @Override
            public void onResponse(Response<tv.ismar.sakura.data.http.Empty> response) {
                Log.i(TAG, "uploadTestResult success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "uploadTestResult");
            }
        });
    }

    /**
     * 将 cursor 转为 list, 因为 在使用 cursor 的时候,可能已经关闭了
     */
    public static List<Integer> cursorToList(Cursor cursor) {
        List<Integer> cdnCollections = new ArrayList<Integer>();
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                cdnCollections.add(cursor.getInt(cursor.getColumnIndex("cdn_id")));
            }
        }

        return cdnCollections;
    }

    enum Status {
        CANCEL,
        COMPLETE
    }
}