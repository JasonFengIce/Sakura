package cn.ismartv.speedtester.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.*;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheLoader;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.HttpDownloadTask;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.HttpDataEntity;
import cn.ismartv.speedtester.data.IpLookUpEntity;
import cn.ismartv.speedtester.data.http.EventInfoEntity;
import cn.ismartv.speedtester.data.http.SpeedLogEntity;
import cn.ismartv.speedtester.data.preferences.UserLocation;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.ui.activity.HomeActivity;
import cn.ismartv.speedtester.ui.activity.HomeActivity.OnKeyEventListener;
import cn.ismartv.speedtester.ui.adapter.NodeListAdapter;
import cn.ismartv.speedtester.ui.widget.SakuraButton;
import cn.ismartv.speedtester.ui.widget.SakuraListView;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.content.ContentProvider;
import com.google.gson.Gson;
import com.ismartv.android.vod.core.Utils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cn.ismartv.speedtester.core.cache.CacheManager.*;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentSpeed extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        HttpDownloadTask.OnCompleteListener, HomeActivity.OnBackPressListener, OnKeyEventListener {
    public static final int ALL_COMPLETE_MSG = 0x0001;
    public static final int ALL_COMPLETE = 0x0002;
    private static final String TAG = "FragmentSpeed";
    public static boolean can = false;
    private static int count = 0;
    /**
     * 正在测速弹出框
     */
    public Dialog speedTestProgressPopup;
    /**
     * 测速完成弹出框
     */

    public PopupWindow selectedCompletePopupWindow;
    public PopupWindow testCompletePopupWindow;
    @InjectView(R.id.node_list)
    SakuraListView nodeList;
    @InjectView(R.id.province_spinner)
    Spinner provinceSpinner;
    @InjectView(R.id.isp_spinner)
    Spinner ispSpinner;
    /**
     * 测试按钮
     */
    @InjectView(R.id.speed_test_btn)
    SakuraButton speedTestBtn;
    @InjectView(R.id.current_node_text)
    TextView currentNode;
    @InjectView(R.id.unbind_node)
    SakuraButton unbindNode;
    boolean running = false;
    SharedPreferences sharedPreferences;
    /**
     * 测速线程
     */
    HttpDownloadTask httpDownloadTask;
    private NodeListAdapter nodeListAdapter;
    private int provincesPosition;
    private int ispPosition;
    private String[] selectionArgs;
    private String[] cities;
    private View temp;
    /**
     * 传入下载中的 CDN 节点 ID
     */
    private List<Integer> cdnCollections;
    /**
     * Activity
     */
    private HomeActivity mActivity;
    /**
     *
     */

    private boolean isPressSpeedButton = false;

    private boolean isFragmentDestroy = false;

    private int selectedOne = 0;

    private int pickNode = -1;

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

    /**
     * 将 cursor 转为 list, 因为 在使用 cursor 的时候,可能已经关闭了
     */
    public static List<Integer> cursorToList(Cursor cursor) {
        List<Integer> cdnCollections = new ArrayList<Integer>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            cdnCollections.add(cursor.getInt(cursor.getColumnIndex(NodeCacheTable.CDN_ID)));
        }
        return cdnCollections;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (HomeActivity) activity;
        mActivity.setBackPressListener(this);
        mActivity.setOnKeyEventListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_speed, container, false);
        ButterKnife.inject(this, mView);

        isFragmentDestroy = false;

        nodeList.setNextFocusDownId(R.id.node_list);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        provinceSpinner.setNextFocusUpId(R.id.unbind_node);
        initSpeedTestProgressDialog();


    }

    @Override
    public void onStart() {
        super.onStart();
        nodeListAdapter = new NodeListAdapter(mActivity, null, true);
        cities = getResources().getStringArray(R.array.citys);
        nodeList.setAdapter(nodeListAdapter);

        ArrayAdapter<CharSequence> provinceSpinnerAdapter = ArrayAdapter.createFromResource(mActivity,
                R.array.citys, R.layout.spinner_text);
        provinceSpinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);

        ArrayAdapter<CharSequence> operatorSpinnerAdapter = ArrayAdapter.createFromResource(mActivity,
                R.array.isps, R.layout.spinner_text);
        operatorSpinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        ispSpinner.setAdapter(operatorSpinnerAdapter);


        if (!mActivity.isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }
        switch (ispPosition) {
            case 4:
                getLoaderManager().initLoader(1, null, this);
                break;
            default:
                getLoaderManager().initLoader(0, null, this);
                break;

        }

        getBindCdn();

        /**
         * according to preference select province and isp
         */

        sharedPreferences = mActivity.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
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
        String selection1 = "area" + "=? and " + "isp" + "=?" + " or flag  <> ?" + " ORDER BY isp,speed DESC limit 5";
        String selection2 = "area" + "=? and " + "isp" + " in (?, ?)" + " or flag  <> ?" + " ORDER BY isp,speed DESC limit 5";
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
        /**
         *给 cdnCollections 赋值
         */
        cdnCollections = cursorToList(cursor);
        nodeListAdapter.swapCursor(cursor);
        //nodeList.setSelection(1);

        if (count == 1 && cursor.getCount() != 0) {
            //firstSpeedTest(cursor);

        }
        count = count + 1;


        if (null != fetchCheck() && null != fetchCheck().nick) {
            currentNode.setText(getText(R.string.current_node) + fetchCheck().nick);
            unbindNode.setText(R.string.switch_to_auto);
            unbindNode.setEnabled(true);
            unbindNode.setBackgroundResource(R.drawable.selector_button);
            if (pickNode != -1) {
                nodeList.setMySelection(pickNode);
                pickNode = -1;

            } else if (speedTestBtn.getText().equals(getString(R.string.net_speed_test))) {
                unbindNode.requestFocus();
                unbindNode.requestFocusFromTouch();
            } else {
                currentNode.requestFocusFromTouch();
                currentNode.requestFocus();
            }


        } else {
            unbindNode.setText(R.string.already_to_auto);
            currentNode.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
            unbindNode.setEnabled(false);
            unbindNode.setBackgroundColor(Color.GRAY);
            if (speedTestBtn.getText().equals(getString(R.string.net_speed_test))) {
                speedTestBtn.requestFocusFromTouch();
                speedTestBtn.requestFocus();
            } else {
                currentNode.requestFocusFromTouch();
                currentNode.requestFocus();
            }

        }

        if (isPressSpeedButton) {
//            nodeList.getChildAt(0).setBackgroundColor(Color.RED);
        } else {

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
        pickNode = position;
        if (AppConstant.DEBUG) {
            Log.d(TAG, "item positon ---> " + position);
            Log.d(TAG, "item tag ---> " + view.getTag() + "   " + parent.getTag());
        }
        CacheManager cacheManager = CacheManager.getInstance(mActivity);
        cacheManager.updatePosition(provincesPosition, ispPosition - 1);

        initPopWindow((Integer) view.getTag(), position);
    }

    @OnItemSelected(R.id.node_list)
    public void selectNode(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onSingleComplete(String cdnID, String nodeName, String speed) {
        uploadTestResult(cdnID, speed);
        SpeedLogEntity speedLog = new SpeedLogEntity();
        speedLog.setCdn_id(cdnID);
        speedLog.setCdn_name(nodeName);
        speedLog.setSpeed(speed);

        SharedPreferences preferences = mActivity.getSharedPreferences("user_location_info", mActivity.MODE_PRIVATE);
        speedLog.setLocation(preferences.getString("user_default_city", ""));
        speedLog.setLocation(preferences.getString("user_default_isp", ""));


        Gson gson = new Gson();
        String data = gson.toJson(speedLog, SpeedLogEntity.class);
        String base64Data = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
        uploadDeviceLog(base64Data);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("hello", "world");
        map.put("hello2", "world2");


        Log.d(TAG, gson.toJson(map, HashMap.class));

    }

    @Override
    public void onAllComplete() {
        if (!isFragmentDestroy) {
            speedTestProgressPopup.dismiss();
            initCompletedPopWindow(Status.COMPLETE);
        }

    }

    @Override
    public void onCancel() {
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

    /**
     * 点击测速按钮,开始测速
     */
    @OnClick(R.id.speed_test_btn)
    public void speedTest() {
//        nodeList.setSelector(R.drawable.list_selector);
        isPressSpeedButton = true;
        mActivity.isFirstSpeedTest = false;
        // nodeList.setSelector(R.drawable.list_selector);
        if (!mActivity.isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }
        speedTestBtn.setBackgroundColor(Color.GRAY);
        speedTestBtn.setEnabled(false);

        speedTestProgressPopup.show();

//        Gson gson = new Gson();
//        HashMap<String, String> map = new HashMap<String, String>();
//        map.put("event", "SPEED_TEST");
//        map.put("time", String.valueOf(System.currentTimeMillis()));
//        EventInfoEntity infoEntity = new EventInfoEntity();
//        infoEntity.setEvent("speed_app_click");
//        infoEntity.setProperties(map);
//        uploadDeviceLog(Base64.encodeToString(gson.toJson(infoEntity, EventInfoEntity.class).getBytes(), Base64.DEFAULT));


    }

    private void initPopWindow(final int cdnID, final int position) {
        View contentView = LayoutInflater.from(mActivity)
                .inflate(R.layout.popup_confirm_node, null);
        contentView.setBackgroundResource(R.drawable.bg_popup);
        final PopupWindow popupWindow = new PopupWindow(null, 600, 180);
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(nodeList, Gravity.CENTER, 0, 0);

        final TextView title = (TextView) contentView.findViewById(R.id.title);

        SakuraButton confirmButton = (SakuraButton) contentView.findViewById(R.id.confirm_btn);
        SakuraButton cancleButton = (SakuraButton) contentView.findViewById(R.id.cancle_btn);

        confirmButton.requestFocusFromTouch();
        confirmButton.requestFocus();


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                bindCdn(String.valueOf(cdnID), mActivity);

            }
        });
        selectedCompletePopupWindow = popupWindow;
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickNode = -1;
                popupWindow.dismiss();
            }
        });


    }

    private Dialog initSpeedTestProgressDialog() {
        Dialog dialog = new Dialog(mActivity, R.style.ProgressDialog);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.width = 400;
        lp.height = 150;
        View mView = LayoutInflater.from(mActivity).inflate(R.layout.popup_test_progress, null);
        dialog.setContentView(mView, lp);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_ESCAPE:
                        dialog.dismiss();
                        initCompletedPopWindow(Status.CANCEL);
                        return true;
                }
                return false;
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                /**
                 * 开始测速
                 */
                httpDownloadTask = new HttpDownloadTask(mActivity);
                httpDownloadTask.setCompleteListener(FragmentSpeed.this);
                httpDownloadTask.execute(cdnCollections);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                /**
                 * 恢复 测速 按钮 颜色
                 */
                speedTestBtn.setBackgroundResource(R.drawable.selector_button);
                speedTestBtn.setEnabled(true);
                httpDownloadTask.cancel(true);
                httpDownloadTask = null;
            }
        });
        speedTestProgressPopup = dialog;
        return dialog;
    }


//

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
                fetchIpLookup();

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
//        Gson gson = new Gson();
//        HashMap<String, String> map = new HashMap<String, String>();
//        map.put("event", "UNBIND_NODE");
//        map.put("time", String.valueOf(System.currentTimeMillis()));
//        EventInfoEntity infoEntity = new EventInfoEntity();
//        infoEntity.setEvent("speed_app_click");
//        infoEntity.setProperties(map);
//        uploadDeviceLog(Base64.encodeToString(gson.toJson(infoEntity, EventInfoEntity.class).getBytes(), Base64.DEFAULT));
    }

    private void fetchIpLookup() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.LILY_HOST)
                .build();

        ClientApi.IpLookUp client = restAdapter.create(ClientApi.IpLookUp.class);
        client.execute(new Callback<IpLookUpEntity>() {
            @Override
            public void success(IpLookUpEntity ipLookUpEntity, Response response) {


                SharedPreferences preferences = mActivity.getSharedPreferences("user_location_info", mActivity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("user_default_city", ipLookUpEntity.getCity());
                editor.putString("user_default_isp", ipLookUpEntity.getIsp());
                editor.apply();


                CacheManager cacheManager = CacheManager.getInstance(mActivity);
                CacheManager.IpLookUp ipLookUp = cacheManager.new IpLookUp();
                ipLookUp.updateIpLookUpCache(ipLookUpEntity);
                provinceSpinner.setSelection(sharedPreferences.getInt(CacheManager.IpLookUp.USER_PROVINCE, 0));
                ispSpinner.setSelection(sharedPreferences.getInt(CacheManager.IpLookUp.USER_ISP, 0));


            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchIpLookup failed!!!");
            }
        });
    }

    private PopupWindow initCompletedPopWindow(final Status status) {
        int titleRes;
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


        currentNode.requestFocusFromTouch();
        currentNode.requestFocus();

        View contentView = LayoutInflater.from(mActivity)
                .inflate(R.layout.popup_test_complete, null);
        /**
         * 标题
         */
        TextView title = (TextView) contentView.findViewById(R.id.complete_title);
        title.setText(titleRes);
        contentView.setBackgroundResource(R.drawable.bg_popup);
        final PopupWindow popupWindow = new PopupWindow(null, 500, 150);
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);

        popupWindow.showAtLocation(nodeList, Gravity.CENTER, 0, 0);

        SakuraButton cancleButton = (SakuraButton) contentView.findViewById(R.id.test_c_confirm_btn);
        cancleButton.requestFocus();
        cancleButton.requestFocusFromTouch();


        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                switch (status) {
                    case CANCEL:
                        speedTestBtn.requestFocus();
                        speedTestBtn.requestFocusFromTouch();
                        break;
                    case COMPLETE:
                        nodeList.setSelectionOne();
                        break;
                }

            }
        });
        testCompletePopupWindow = popupWindow;

        return popupWindow;
    }

    public boolean getTaskStatusIsCancelled() {
        if (null == httpDownloadTask)
            return true;
        return httpDownloadTask.isCancelled();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentDestroy = true;
        if (null != httpDownloadTask && !httpDownloadTask.isCancelled()) {
            httpDownloadTask.cancel(true);
        }
        if (AppConstant.DEBUG)
            Log.d(TAG, "onDestroyView");
    }

    @Override
    public void backPress() {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (null != testCompletePopupWindow && testCompletePopupWindow.isShowing())
                    testCompletePopupWindow.dismiss();

                break;
        }
        return true;
    }


    enum Status {
        CANCEL,
        COMPLETE
    }


    private void uploadDeviceLog(String data) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.LOG_HOST)
                .build();
        ClientApi.DeviceLog client = restAdapter.create(ClientApi.DeviceLog.class);
        String sn = DeviceUtils.getSnCode();
        String modelName = DeviceUtils.getModel();
        client.execute(data, sn, modelName, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }
}


