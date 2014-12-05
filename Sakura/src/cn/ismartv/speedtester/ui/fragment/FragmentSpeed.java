package cn.ismartv.speedtester.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.*;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheLoader;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.DownloadTask;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.HttpDataEntity;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.ui.activity.HomeActivity;
import cn.ismartv.speedtester.ui.adapter.NodeListAdapter;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.content.ContentProvider;
import com.ismartv.android.vod.core.Utils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static cn.ismartv.speedtester.core.cache.CacheManager.*;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentSpeed extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, DownloadTask.OnSpeedTestListener {
    private static final String TAG = "FragmentSpeed";
    private static int count = 0;
    public Dialog testProgressPopup;
    @InjectView(R.id.node_list)
    ListView nodeList;
    @InjectView(R.id.province_spinner)
    Spinner provinceSpinner;
    @InjectView(R.id.isp_spinner)
    Spinner ispSpinner;
    @InjectView(R.id.speed_test_btn)
    TextView speedTestBtn;
    @InjectView(R.id.current_node_text)
    TextView currentNode;
    @InjectView(R.id.unbind_node)
    Button unbindNode;
    boolean running = false;
    DownloadTask downloadTask;
    private NodeListAdapter nodeListAdapter;
    private int provincesPosition;
    private int ispPosition;
    private String[] selectionArgs;
    private String[] cities;

    public static final int ALL_COMPLETE_MSG = 0x0001;

    public MessageHandler messageHandler;

    /**
     * Activity
     */
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageHandler = new MessageHandler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_speed, container, false);
        ButterKnife.inject(this, mView);
        return mView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        nodeListAdapter = new NodeListAdapter(mActivity, null, true);
        cities = getResources().getStringArray(R.array.citys);
        nodeList.setAdapter(nodeListAdapter);

        ArrayAdapter<CharSequence> provinceSpinnerAdapter = ArrayAdapter.createFromResource(mActivity,
                R.array.citys, R.layout.spinner_text);
        provinceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);

        ArrayAdapter<CharSequence> operatorSpinnerAdapter = ArrayAdapter.createFromResource(mActivity,
                R.array.isps, R.layout.spinner_text);
        operatorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ispSpinner.setAdapter(operatorSpinnerAdapter);


        if (!((HomeActivity) mActivity).isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }
        getLoaderManager().initLoader(0, null, this);
        getBindCdn();

        /**
         * according to preference select province and isp
         */

        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getInt(CacheManager.IpLookUp.USER_PROVINCE, -1) == -1 || sharedPreferences.getInt(CacheManager.IpLookUp.USER_ISP, -1) == -1) {
//            fetchIpLookup();
        } else {
            provinceSpinner.setSelection(sharedPreferences.getInt(CacheManager.IpLookUp.USER_PROVINCE, 0));
            ispSpinner.setSelection(sharedPreferences.getInt(CacheManager.IpLookUp.USER_ISP, 0));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getBindCdn();
    }

    @Override
    public void onPause() {
        super.onPause();
        count = 0;
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int flag, Bundle bundle) {
        String selection1 = "area" + "=? and " + "isp" + "=?" + " or flag  <> ?" + " ORDER BY isp,speed DESC";
        String selection2 = "area" + "=? and " + "isp" + " in (?, ?)" + " or flag  <> ?" + " ORDER BY isp,speed DESC";
        CacheLoader cacheLoader = new CacheLoader(mActivity, ContentProvider.createUri(NodeCacheTable.class, null),
                null,
                null, null, null);
        switch (flag) {
            case 0:
                cacheLoader.setSelection(selection1);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            case 1:
                cacheLoader.setSelection(selection2);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            default:
                break;
        }
        return cacheLoader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        if (AppConstant.DEBUG)
            Log.d(TAG, "onLoadFinished");
        nodeListAdapter.swapCursor(cursor);

        if (count == 1 && cursor.getCount() != 0) {
//            firstSpeedTest(cursor);

        }
        count = count + 1;

        if (null != fetchCheck() && null != fetchCheck().nick) {
            currentNode.setText(getText(R.string.current_node) + fetchCheck().nick);
            unbindNode.setEnabled(true);
            unbindNode.setBackgroundResource(R.drawable.selector_button);
        } else {
            currentNode.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
            unbindNode.setEnabled(false);
            unbindNode.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        nodeListAdapter.swapCursor(null);
    }

    @OnItemSelected(R.id.province_spinner)
    public void pickProvince(AdapterView<?> parent, View view, int position, long id) {
        provincesPosition = position;
        notifiySourceChanged();
    }

    @OnItemSelected(R.id.isp_spinner)
    public void pickIsp(AdapterView<?> parent, View view, int position, long id) {
        ispPosition = position + 1;
        notifiySourceChanged();
    }

    @OnItemClick(R.id.node_list)
    public void pickNode(AdapterView<?> parent, View view, int position, long id) {


        if (AppConstant.DEBUG) {
            Log.d(TAG, "item positon ---> " + position);
            Log.d(TAG, "item tag ---> " + view.getTag() + "   " + parent.getTag());
        }
        initPopWindow((Integer) view.getTag());
    }

    private void notifiySourceChanged() {
        if (ispPosition == 4) {
            selectionArgs = new String[]{String.valueOf(StringUtils.getAreaCodeByProvince(cities[provincesPosition])),
                    String.valueOf(2), String.valueOf(3), "0"};
            getLoaderManager().destroyLoader(0);
            getLoaderManager().restartLoader(1, null, FragmentSpeed.this).forceLoad();
        } else {
            selectionArgs = new String[]{String.valueOf(StringUtils.getAreaCodeByProvince(cities[provincesPosition])),
                    String.valueOf(ispPosition), "0"};
            getLoaderManager().destroyLoader(1);
            getLoaderManager().restartLoader(0, null, FragmentSpeed.this).forceLoad();
        }
    }


    @OnClick(R.id.speed_test_btn)
    public void speedTest() {
        ((HomeActivity) mActivity).isFirstSpeedTest = false;
        if (!((HomeActivity) mActivity).isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }
        testProgressPopup = initTestProgressDialog();
        /**
         * update position
         */
        CacheManager cacheManager = CacheManager.getInstance(mActivity);
        cacheManager.updatePosition(provincesPosition, ispPosition - 1);

        downloadTask = new DownloadTask(mActivity, nodeListAdapter.getCursor());
        downloadTask.setSpeedTestListener(this);
        downloadTask.start();

        speedTestBtn.setBackgroundColor(Color.GRAY);
        speedTestBtn.setEnabled(false);
    }

    @Override
    public void changeStatus(String recordId, String cdnId, boolean status) {

    }

    @Override
    public void compelte(String recordId, String cdnid, int speed) {
        updateNodeCache(Integer.parseInt(recordId), Integer.parseInt(cdnid), speed);
        uploadTestResult(cdnid, String.valueOf(speed));
    }

    @Override
    public void allCompelte() {
        messageHandler.sendEmptyMessage(ALL_COMPLETE_MSG);
    }

    private void initPopWindow(final int cdnID) {
        View contentView = LayoutInflater.from(mActivity)
                .inflate(R.layout.popup_confirm_node, null);
        contentView.setBackgroundResource(R.drawable.bg_popup);
        final PopupWindow popupWindow = new PopupWindow(null, 500, 150);
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(nodeList, Gravity.CENTER, 0, 0);

        TextView confirmButton = (TextView) contentView.findViewById(R.id.confirm_btn);
        TextView cancleButton = (TextView) contentView.findViewById(R.id.cancle_btn);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                bindCdn(String.valueOf(cdnID), mActivity);
            }
        });
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });


    }


    private Dialog initTestProgressDialog() {
        Dialog dialog = new Dialog(mActivity, R.style.ProgressDialog);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 400;
        lp.height = 150;
        View mView = LayoutInflater.from(mActivity).inflate(R.layout.popup_test_progress, null);
        dialog.setContentView(mView, lp);
        dialog.show();
        return dialog;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    @OnClick(R.id.unbind_node)
    public void unbindNode() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.UnbindNode client = restAdapter.create(ClientApi.UnbindNode.class);
        String sn;
        if ("unknown".equals(DeviceUtils.getSnCode()))
            sn = "other";
        else
            sn = DeviceUtils.getSnCode();

        client.excute(ClientApi.UnbindNode.ACTION, sn, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {

                clearCheck();
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    public static void uploadTestResult(String cdnId, String speed) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.UploadResult client = restAdapter.create(ClientApi.UploadResult.class);
        client.excute("submitTestData", DeviceUtils.getSnCode(), cdnId, speed, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    public static void getBindCdn() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.GetBindCdn client = restAdapter.create(ClientApi.GetBindCdn.class);
        String sn;
        if ("unknown".equals(DeviceUtils.getSnCode()))
            sn = "other";
        else
            sn = DeviceUtils.getSnCode();
        client.excute("getBindcdn", sn, new Callback<HttpDataEntity>() {
            @Override
            public void success(HttpDataEntity httpData, Response response) {
                String result = Utils.getResult(response);
                Log.d(TAG, result);
                if ("104".equals(httpData.getRetcode())) {
                    return;
                } else {
                    updateCheck(httpData.getSncdn().getCdnid(), true);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    private static void bindCdn(final String cdn, final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.BindCdn client = restAdapter.create(ClientApi.BindCdn.class);

        String sn;
        if ("unknown".equals(DeviceUtils.getSnCode()))
            sn = "other";
        else
            sn = DeviceUtils.getSnCode();

        client.excute("bindecdn", sn, cdn, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {

                Toast.makeText(context, R.string.node_bind_success, Toast.LENGTH_LONG).show();
                getBindCdn();
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALL_COMPLETE_MSG:
                    speedTestBtn.setEnabled(true);
                    speedTestBtn.setClickable(true);
                    speedTestBtn.setBackgroundResource(R.drawable.selector_button);
                    if (null != testProgressPopup && testProgressPopup.isShowing()) {
                        testProgressPopup.dismiss();
                    }

                    break;
                default:
                    break;
            }
        }
    }
}


