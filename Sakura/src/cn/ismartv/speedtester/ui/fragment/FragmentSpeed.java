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
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheLoader;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.DownloadTask;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.ui.adapter.NodeListAdapter;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.content.ContentProvider;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentSpeed extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, DownloadTask.OnSpeedTestListener {
    private static final String TAG = "FragmentSpeed";

    private NodeListAdapter nodeListAdapter;

    @InjectView(R.id.node_list)
    ListView nodeList;

    @InjectView(R.id.province_spinner)
    Spinner provinceSpinner;
    @InjectView(R.id.isp_spinner)
    Spinner ispSpinner;


    @InjectView(R.id.speed_test_btn)
    Button speedTestBtn;

    public PopupWindow testProgressPopup;


    private int provincesPosition;
    private int ispPosition;


    private String[] selectionArgs;

    private String[] cities;

    boolean running = false;
    DownloadTask downloadTask;
    private static int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodeListAdapter = new NodeListAdapter(getActivity(), null, true);
        getLoaderManager().initLoader(0, null, this);
        cities = getResources().getStringArray(R.array.citys);
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
    }


    @OnItemClick(R.id.node_list)
    public void pickNode(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "item positon ---> " + position);
        initPopWindow((Integer) view.getTag());
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int flag, Bundle bundle) {
        String selection1 = "area" + "=? and " + "isp" + "=?";
        String selection2 = "area" + "=? and " + "isp" + " in (?, ?)";
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

        if (count == 1 && cursor.getCount()!=0) {
            firstSpeedTest(cursor);

        }
        count = count + 1;
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
                    String.valueOf(2), String.valueOf(3)};
            getLoaderManager().destroyLoader(0);
            getLoaderManager().restartLoader(1, null, FragmentSpeed.this).forceLoad();
        } else {
            selectionArgs = new String[]{String.valueOf(StringUtils.getAreaCodeByProvince(cities[provincesPosition])),
                    String.valueOf(ispPosition)};
            getLoaderManager().destroyLoader(1);
            getLoaderManager().restartLoader(0, null, FragmentSpeed.this).forceLoad();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        count = 0;
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

    @OnClick(R.id.speed_test_btn)
    public void speedTest() {
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


    private static void bindCdn(final Context context, final String cdn) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.NONE)
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
//                Toast.makeText(context, "success!!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private void initPopWindow(final int cdnID) {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.popup_confirm_node, null);
        contentView.setBackgroundResource(R.drawable.bg_popup);
        final PopupWindow popupWindow = new PopupWindow(null, 400, 150);
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(nodeList, Gravity.CENTER, 0, 0);

        ImageView button = (ImageView) contentView.findViewById(R.id.confirm_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                bindCdn(getContext(), String.valueOf(cdnID));
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


    private class showProgressViewRunnable implements Runnable {
        @Override
        public void run() {
            testProgressPopup = initTestProgressPopWindow();
        }
    }


    private void firstSpeedTest(final Cursor cursor) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("launched", false)) {
            CacheManager.updateLaunched(getActivity(), true);
            nodeList.post(new showProgressViewRunnable());
            new Thread() {
                @Override
                public void run() {
                    downloadTask = new DownloadTask(getActivity(), cursor);
                    downloadTask.setSpeedTestListener(FragmentSpeed.this);
                    downloadTask.start();
                }
            }.start();

        }
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }


}


