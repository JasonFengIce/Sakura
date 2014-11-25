package cn.ismartv.speedtester.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.*;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.ui.activity.HomeActivity;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheLoader;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.DownloadTask;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.HttpDataEntity;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.ui.adapter.NodeListAdapter;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.ismartv.android.vod.core.Utils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentSpeed extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, DownloadTask.OnSpeedTestListener {


    private static final String TAG = "FragmentSpeed";
    private static int count = 0;
    public PopupWindow testProgressPopup;
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

    private Context context;

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
                    CacheManager.updateCheck(httpData.getSncdn().getCdnid(), true);
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        nodeListAdapter = new NodeListAdapter(getActivity(), null, true);
        cities = getResources().getStringArray(R.array.citys);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_speed, container, false);
        ButterKnife.inject(this, mView);

//        ispSpinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    nodeList.setSelection(-1);
//                }
//            }
//        });

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nodeList.setAdapter(nodeListAdapter);

        ArrayAdapter<CharSequence> provinceSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.citys, R.layout.spinner_text);
        provinceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);


        ArrayAdapter<CharSequence> operatorSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.isps, R.layout.spinner_text);
        operatorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ispSpinner.setAdapter(operatorSpinnerAdapter);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("areadly_fetch_net", false)) {
            int cityCache = sharedPreferences.getInt("l_city_position", 0);
            int ispCache = sharedPreferences.getInt("l_isp_position", 0);
            provinceSpinner.setSelection(cityCache);
            ispSpinner.setSelection(ispCache);

        } else {
            int cityCache = sharedPreferences.getInt("city_position", 0);
            int ispCache = sharedPreferences.getInt("isp_position", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("areadly_fetch_net", true);
            editor.apply();
            CacheManager.updateNodePosition(getContext(), cityCache, ispCache);
            provinceSpinner.setSelection(cityCache);
            ispSpinner.setSelection(ispCache);
        }

        if (!((HomeActivity) getActivity()).isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }
        getLoaderManager().initLoader(0, null, this);
        getBindCdn();
    }

    @Override
    public void onResume() {
        super.onResume();
        getBindCdn();
    }

    @OnItemClick(R.id.node_list)
    public void pickNode(AdapterView<?> parent, View view, int position, long id) {


        if (AppConstant.DEBUG) {
            Log.d(TAG, "item positon ---> " + position);
            Log.d(TAG, "item tag ---> " + view.getTag() + "   " + parent.getTag());
        }
        initPopWindow((Integer) view.getTag());
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int flag, Bundle bundle) {
        String selection1 = "area" + "=? and " + "isp" + "=?" + " or flag  <> ?" + " ORDER BY isp,speed DESC";
        String selection2 = "area" + "=? and " + "isp" + " in (?, ?)" + " or flag  <> ?" + " ORDER BY isp,speed DESC";
        CacheLoader cacheLoader = new CacheLoader(getActivity(), ContentProvider.createUri(NodeCacheTable.class, null),
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

        if (null != CacheManager.fetchCheck() && null != CacheManager.fetchCheck().nick)
            currentNode.setText(getText(R.string.current_node) + CacheManager.fetchCheck().nick);
        else
            currentNode.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
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

    @Override
    public void onPause() {
        super.onPause();
        count = 0;
    }

    @OnClick(R.id.speed_test_btn)
    public void speedTest() {
        ((HomeActivity) getActivity()).isFirstSpeedTest = false;
        if (!((HomeActivity) getActivity()).isFirstSpeedTest) {
            speedTestBtn.setText(R.string.button_label_retest);
        }

        testProgressPopup = initTestProgressPopWindow();
        CacheManager.updateNodePosition(getContext(), provincesPosition, ispPosition - 1);
        downloadTask = new DownloadTask(getActivity(), nodeListAdapter.getCursor());
        downloadTask.setSpeedTestListener(this);
        downloadTask.start();
    }

    @Override
    public void changeStatus(String recordId, String cdnId, boolean status) {

    }

    @Override
    public void compelte(String recordId, String cdnid, int speed) {
        CacheManager.updateNodeCache(Integer.parseInt(recordId), Integer.parseInt(cdnid), speed);
        uploadTestResult(cdnid, String.valueOf(speed));
    }

    @Override
    public void allCompelte() {
        if (null != testProgressPopup)
            testProgressPopup.dismiss();
    }

    private void initPopWindow(final int cdnID) {
        View contentView = LayoutInflater.from(getActivity())
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
                bindCdn(String.valueOf(cdnID), context);
            }
        });
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });


    }

    private Context getContext() {
        return getActivity();
    }

    private PopupWindow initTestProgressPopWindow() {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.popup_test_progress, null);
        contentView.setBackgroundColor(0x99525252);
        final PopupWindow popupWindow = new PopupWindow(null, 400, 150);
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(false);
        popupWindow.showAtLocation(nodeList, Gravity.CENTER, 0, 0);
        return popupWindow;
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

                CacheManager.clearCheck();
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }
}


