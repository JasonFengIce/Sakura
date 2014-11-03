package cn.ismartv.speedtester.ui.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
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
import com.activeandroid.util.Log;
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


    private int provincesPosition;
    private int ispPosition;


    private String[] selectionArgs;

    private String[] cities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodeListAdapter = new NodeListAdapter(getActivity(), null, true);
        getLoaderManager().initLoader(0, null, this);
        cities = getResources().getStringArray(R.array.citys);


        Log.d(TAG, "onCreateLoader");
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

        nodeList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        nodeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "item positon ---> " + position);
//                initPopWindow();
            }
        });

    }

//
//    @OnItemClick(R.id.node_list)
//    public void pickNode(AdapterView<?> parent, View view, int position, long id) {
//        Log.d(TAG, "item positon ---> " + position);
//        initPopWindow();
//
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int flag, Bundle bundle) {
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
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        nodeListAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
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

    public static void uploadTestResult(String cdnId, String speed) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.NONE)
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
        DownloadTask downloadTask = new DownloadTask(getActivity(), nodeListAdapter.getCursor());
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


            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });


    }


    private void initPopWindow() {
        // 加载PopupWindow的布局文件
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.popup_confirm_node, null);
        // 设置PopupWindow的背景颜色
        contentView.setBackgroundColor(Color.RED);
        // 声明一个对话框
        final PopupWindow popupWindow = new PopupWindow(null, 200, 300);
        // 为自定义的对话框设置自定义布局
        popupWindow.setContentView(contentView);

        // 这个popupWindow.setFocusable(true);非常重要，如果不在弹出之前加上这条语句，你会很悲剧的发现，你是无法在
        //
        // editText中输入任何东西的。该方法可以设定popupWindow获取焦点的能力。当设置为true时，系统会捕获到焦点给popupWindow
        //
        // 上的组件。默认为false哦.该方法一定要在弹出对话框之前进行调用。
        popupWindow.setFocusable(false);

        // popupWindow.showAsDropDown（View view）弹出对话框，位置在紧挨着view组件
        //
        // showAsDropDown(View anchor, int xoff, int yoff)弹出对话框，位置在紧挨着view组件，x y
        // 代表着偏移量
        //
        // showAtLocation(View parent, int gravity, int x, int y)弹出对话框
        //
        // parent 父布局 gravity 依靠父布局的位置如Gravity.CENTER x y 坐标值

//        popupWindow.showAsDropDown(button);
//
//        final EditText editText = (EditText) contentView
//                .findViewById(R.id.editText1);
//        // 设定当你点击EditText时，弹出的输入框是啥样子的。这里设置默认数字是输入，非数字不能输入
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

//        Button button_sure = (Button) contentView
//                .findViewById(R.id.button1_sure);
//        button_sure.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                popupWindow.dismiss();
//                textView.setText("展示信息:" + editText.getText());
//            }
//        });
//        Button button_cancel = (Button) contentView
//                .findViewById(R.id.button2_cancel);
//        button_cancel.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                popupWindow.dismiss();
//            }
//        });
    }


}
