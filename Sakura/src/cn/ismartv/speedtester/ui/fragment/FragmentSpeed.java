package cn.ismartv.speedtester.ui.fragment;

import static cn.ismartv.speedtester.core.cache.CacheManager.clearCheck;
import static cn.ismartv.speedtester.core.cache.CacheManager.fetchCheck;
import static cn.ismartv.speedtester.core.cache.CacheManager.updateCheck;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheLoader;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.HttpDownloadTask;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.HttpDataEntity;
import cn.ismartv.speedtester.data.IpLookUpEntity;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.ui.activity.HomeActivity;
import cn.ismartv.speedtester.ui.activity.HomeActivity.OnKeyEventListener;
import cn.ismartv.speedtester.ui.adapter.NodeListAdapter;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;

import com.activeandroid.content.ContentProvider;
import com.ismartv.android.vod.core.Utils;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentSpeed extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        HttpDownloadTask.OnCompleteListener, HomeActivity.OnBackPressListener, View.OnHoverListener, OnKeyEventListener {
    private static final String TAG = "FragmentSpeed";
    private static int count = 0;


    @InjectView(R.id.node_list)
    ListView nodeList;
    @InjectView(R.id.province_spinner)
    Spinner provinceSpinner;
    @InjectView(R.id.isp_spinner)
    Spinner ispSpinner;

    /**
     * 测试按钮
     */
    @InjectView(R.id.speed_test_btn)
    TextView speedTestBtn;
    @InjectView(R.id.current_node_text)
    TextView currentNode;
    @InjectView(R.id.unbind_node)
    Button unbindNode;

    /**
     * 正在测速弹出框
     */
    public Dialog speedTestProgressPopup;

    /**
     * 测速完成弹出框
     */

    public PopupWindow selectedCompletePopupWindow;

    public PopupWindow testCompletePopupWindow;


    boolean running = false;
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

    public static final int ALL_COMPLETE_MSG = 0x0001;
    public static final int ALL_COMPLETE = 0x0002;


    public static boolean can = false;

    /**
     * Activity
     */
    private HomeActivity mActivity;

    SharedPreferences sharedPreferences;

    /**
     * 测速线程
     */
    HttpDownloadTask httpDownloadTask;

    /**
     *
     */

    private boolean isPressSpeedButton = false;

    private boolean isFragmentDestroy = false;

    private int selectedOne = 0;


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

//        //set onHover Listener
        provinceSpinner.setOnHoverListener(this);
        ispSpinner.setOnHoverListener(this);
        speedTestBtn.setOnHoverListener(this);
        unbindNode.setOnHoverListener(this);
        nodeList.setOnHoverListener(this);
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


        if (!((HomeActivity) mActivity).isFirstSpeedTest) {
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
        } else {
            unbindNode.setText(R.string.already_to_auto);
            currentNode.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
            unbindNode.setEnabled(false);
            unbindNode.setBackgroundColor(Color.GRAY);
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
//        if (null!= view) {
//            TextView textView = (TextView) view;
//            textView.setTextSize(30);
//        }
        provincesPosition = position;
        notifiySourceChanged();
    }

    @OnItemSelected(R.id.isp_spinner)
    public void pickIsp(AdapterView<?> parent, View view, int position, long id) {
//        if (null!= view) {
//            TextView textView = (TextView) view;
//            textView.setTextSize(30);
//        }
        ispPosition = position + 1;
        notifiySourceChanged();
    }

    @OnItemClick(R.id.node_list)
    public void pickNode(AdapterView<?> parent, View view, int position, long id) {

//        nodeList.setSelector(R.drawable.list_selector);
		if (temp == null) {
			view.setBackgroundResource(android.R.drawable.list_selector_background);
			temp = view;
		}else{
			temp.setBackgroundResource(android.R.drawable.list_selector_background);
			view.setBackgroundResource(R.drawable.list_selector);
			temp = view;
		}
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
        Log.d(TAG, "select position is " + position);
//        nodeList.setSelector(R.drawable.list_selector);
		if (temp == null) {
			view.setBackgroundResource(android.R.drawable.list_selector_background);
			temp = view;
		}else{
			temp.setBackgroundResource(android.R.drawable.list_selector_background);
			view.setBackgroundResource(R.drawable.list_selector);
			temp = view;
		}
//        if (position == 0 && selectedOne == 0) {
//            selectedOne += 1;
//            nodeList.getChildAt(0).setBackgroundColor(0xffffff);
//        }else if (position==0&&selectedOne!=0){
//            nodeList.getChildAt(0).setBackgroundResource(R.drawable.list_selector);
//        }

    }


    @Override
    public void onSingleComplete(String cdnID, String speed) {
        uploadTestResult(cdnID, speed);
    }

    @Override
    public void onAllComplete() {
        if (!isFragmentDestroy) {
            speedTestProgressPopup.dismiss();
            initCompletedPopWindow(R.string.test_complete_text);

        }

    }


    @Override
    public void onCancel() {
//        if (!isFragmentDestroy)
//            initCompletedPopWindow(R.string.test_interupt);
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
        nodeList.setSelector(R.drawable.list_selector);
        isPressSpeedButton = true;
        mActivity.isFirstSpeedTest = false;
        // nodeList.setSelector(R.drawable.list_selector);
        if (!((HomeActivity) mActivity).isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }
        speedTestBtn.setBackgroundColor(Color.GRAY);
        speedTestBtn.setEnabled(false);

        speedTestProgressPopup.show();


    }


    private void initPopWindow(final int cdnID, final int position) {
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
        selectedCompletePopupWindow = popupWindow;
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        initCompletedPopWindow(R.string.test_interupt);
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


//


    private void fetchIpLookup() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.LILY_HOST)
                .build();

        ClientApi.IpLookUp client = restAdapter.create(ClientApi.IpLookUp.class);
        client.execute(new Callback<IpLookUpEntity>() {
            @Override
            public void success(IpLookUpEntity ipLookUpEntity, Response response) {
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

    private PopupWindow initCompletedPopWindow(int titleRes) {
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

        TextView cancleButton = (TextView) contentView.findViewById(R.id.test_c_confirm_btn);
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        testCompletePopupWindow = popupWindow;

        return popupWindow;
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
    public boolean onHover(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:

			currentNode.requestFocus();
			currentNode.requestFocusFromTouch();

			switch (view.getId()) {

			case R.id.province_spinner:
			case R.id.isp_spinner:
				view.setBackgroundResource(R.drawable.spinner_ab_focused_holo_dark_am);
				if (temp != null)
					temp.setBackgroundResource(android.R.drawable.list_selector_background);
				break;
			case R.id.unbind_node:
			case R.id.speed_test_btn:
				view.setBackgroundResource(R.drawable.button_focus);
				if (temp != null)
					temp.setBackgroundResource(android.R.drawable.list_selector_background);
				break;
			case R.id.node_list:
				if (temp != null){
					temp.setBackgroundResource(R.drawable.list_selector);
					nodeList.requestFocus();
				}
				break;
			}
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			view.clearFocus();
			switch (view.getId()) {
			case R.id.province_spinner:
			case R.id.isp_spinner:
				view.setBackgroundResource(R.drawable.selector_spinner);
				break;
			case R.id.unbind_node:
			case R.id.speed_test_btn:
				view.setBackgroundResource(R.drawable.selector_button);
				break;
			case R.id.node_list:
				if (temp != null)
					temp.setBackgroundResource(android.R.drawable.list_selector_background);
				break;
			default:
				break;
			}
		default:
			break;
		}

        return true;
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
}


