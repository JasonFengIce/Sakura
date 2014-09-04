package cn.ismartv.speedtester.ui.fragment;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.Message;
import cn.ismartv.speedtester.core.cache.CacheLoader;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.DownloadTask;
import cn.ismartv.speedtester.core.httpclient.CdnClient;
import cn.ismartv.speedtester.core.httpclient.Utils;
import cn.ismartv.speedtester.data.HttpData;
import cn.ismartv.speedtester.provider.NodeCache;
import cn.ismartv.speedtester.ui.adapter.NodeListAdapter;
import cn.ismartv.speedtester.ui.widget.dialog.UpdateAlertDialog;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.StringUtilities;
import cn.ismartv.speedtester.utils.Utilities;
import com.google.gson.Gson;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.List;

/**
 * Created by fenghb on 14-6-25.
 */
public class NodeFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "NodeFragment";

    //view
    private GridView nodes;
    private Spinner citySpinner;
    private Spinner operatorSpinner;
    private Spinner listSpinner;
    private Button speedTestBtn;
    private TextView currentNode;
    private TextView snCode;

    //BroadcastReceiver
    private MessageReceiver messageReceiver;


    private List<HashMap<String, String>> list;
    private NodeListAdapter nodeListAdapter;

    private String[] selectionArgs;
    private String[] projection = null;

    private int cityPosition, operatorPosition, listPosition;

    DownloadTask downloadTask;

    boolean running = false;

    public static Handler messagHandler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //receiver
        messageReceiver = new MessageReceiver();
        messagHandler = new Handler();

        nodeListAdapter = new NodeListAdapter(getActivity(), null, true);
        initLoader();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Message.ACTION);
        getActivity().registerReceiver(messageReceiver, filter);
    }

    //init loader
    private void initLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node, null);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //city
        citySpinner = (Spinner) view.findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> citySpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.citys, android.R.layout.simple_spinner_item);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(citySpinnerAdapter);
        citySpinner.setOnItemSelectedListener(this);

        //operator
        operatorSpinner = (Spinner) view.findViewById(R.id.operator);
        ArrayAdapter<CharSequence> operatorSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.operators, android.R.layout.simple_spinner_item);
        operatorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operatorSpinner.setAdapter(operatorSpinnerAdapter);
        operatorSpinner.setOnItemSelectedListener(this);
        //list
        listSpinner = (Spinner) view.findViewById(R.id.list);
        ArrayAdapter<CharSequence> listSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.lists, android.R.layout.simple_spinner_item);
        listSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listSpinner.setAdapter(listSpinnerAdapter);
        listSpinner.setOnItemSelectedListener(this);


        //speed test button
        speedTestBtn = (Button) view.findViewById(R.id.speed_test_btn);
        speedTestBtn.setOnClickListener(this);

        //current node
        currentNode = (TextView) view.findViewById(R.id.current_node_text);
        nodeListAdapter.setCurrentNode(currentNode);

        //node list
        nodes = (GridView) view.findViewById(R.id.node_list_view);
        nodes.setAdapter(nodeListAdapter);
//        nodes.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        nodes.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        nodes.setOnItemClickListener(this);
//        nodes.setFocusable(false);
        nodes.setOnItemSelectedListener(this);
        nodes.setSelector(new ColorDrawable(getResources().getColor(R.color.node_item_focus)));


        //sn code
        snCode = (TextView) view.findViewById(R.id.sn_code);
        String sn = DevicesUtilities.getSNCode();
        if ("1".equals(sn)) {
            snCode.append(sn + getString(R.string.factory_device));
        } else {
            snCode.append(sn);
        }


        SharedPreferences preferences = getActivity().getSharedPreferences("sakura", Context.MODE_PRIVATE);
        if (preferences.getInt("update", 0) == 1) {
            showUpdate();
//            showNewUpadteDialog();
        }

    }

    //----------------loader----------------
    @Override
    public Loader onCreateLoader(int flag, Bundle bundle) {
        String selection = NodeCache.AREA + "=? and " + NodeCache.OPERATOR + "=?";
        String selection2 = NodeCache.OPERATOR + "=?";
        CacheLoader cacheLoader = new CacheLoader(getActivity(), NodeCache.CONTENT_URI,
                null,
                null, null, null);
        switch (flag) {
            case 0:
                cacheLoader.setSelection(selection);
                cacheLoader.setSelectionArgs(selectionArgs);
                return cacheLoader;
            case 1:
                cacheLoader.setSelection(selection2);
                cacheLoader.setSelectionArgs(selectionArgs);
                return cacheLoader;
            case 2:
                cacheLoader.setSelection(null);
                cacheLoader.setSelectionArgs(null);
                return cacheLoader;
            default:
                break;
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {


        nodeListAdapter.swapCursor(cursor);
        setCurrentNode();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        nodeListAdapter.swapCursor(null);
    }

    //------------------------------------------------------------

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.speed_test_btn:
                CacheManager.updateSelectNodeCache(getActivity(), cityPosition, operatorPosition);

                if (!running) {
                    speedTest(true);
                    running = true;
                    speedTestBtn.setText(R.string.pause);

                } else {
                    speedTest(false);
                    running = false;
                    speedTestBtn.setText(R.string.test);
                }
                break;
            default:
                break;
        }
    }


    //On Item Selected Listener
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        if (adapterView.getId() == R.id.node_list_view) {
            for (int i = 0; i < adapterView.getCount(); i++) {
                View mView = adapterView.getChildAt(i);
                if (null != mView) {
                    Button button = (Button) mView.findViewById(R.id.select_btn);
                    button.setBackgroundColor(Color.WHITE);
                }

            }


            View item = adapterView.getSelectedView();

            Button button = (Button) item.findViewById(R.id.select_btn);
            button.setBackgroundColor(getResources().getColor(R.color.button_bg));
        }


        Log.d(TAG, "position : " + position);
        String[] cities = getResources().getStringArray(R.array.citys);
        switch (adapterView.getId()) {
            case R.id.city_spinner:
                Log.d(TAG, "position city_spinner : " + position);
                cityPosition = position;
                selectionArgs = new String[]{String.valueOf(StringUtilities.getAreaCodeByProvince(cities[cityPosition])),
                        String.valueOf(operatorPosition + 1)};
                getLoaderManager().restartLoader(0, null, this).forceLoad();
                break;
            case R.id.operator:
                operatorPosition = position;
                selectionArgs = new String[]{String.valueOf(StringUtilities.getAreaCodeByProvince(cities[cityPosition])),
                        String.valueOf(operatorPosition + 1)};
                getLoaderManager().restartLoader(0, null, this).forceLoad();
                break;
            case R.id.list:
                listPosition = position;
                switch (listPosition) {
                    case 0:
                        operatorPosition = position;
                        selectionArgs = new String[]{String.valueOf(StringUtilities.getAreaCodeByProvince(cities[cityPosition])),
                                String.valueOf(operatorPosition + 1)};
                        getLoaderManager().destroyLoader(1);
                        getLoaderManager().destroyLoader(2);
                        getLoaderManager().restartLoader(0, null, this).forceLoad();
                        break;
                    case 1:
                        selectionArgs = new String[]{String.valueOf(operatorPosition + 1)};
                        getLoaderManager().destroyLoader(0);
                        getLoaderManager().destroyLoader(2);
                        getLoaderManager().restartLoader(1, null, this).forceLoad();
                        break;
                    case 2:
                        getLoaderManager().destroyLoader(0);
                        getLoaderManager().destroyLoader(1);
                        getLoaderManager().restartLoader(2, null, this).forceLoad();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    //------------------------------------------------------------


    private void speedTest(boolean running) {
        if (running) {
            downloadTask = new DownloadTask(getActivity(), nodeListAdapter.getCursor());
            downloadTask.start();
        } else {
            downloadTask.setRunning(running);
            downloadTask = null;
        }
    }


    private void setCurrentNode() {
        Cursor cursor = getActivity().getContentResolver().query(NodeCache.CONTENT_URI,
                new String[]{NodeCache.NICK}, NodeCache.CHECKED + "=?", new String[]{"true"}, null);
        cursor.moveToFirst();

        //start this app on first time, database is null
        try {

            String nodeNick = null;
            if (null == cursor.getString(cursor.getColumnIndex(NodeCache.NICK))) {
                nodeNick = "";
            } else {
                nodeNick = cursor.getString(cursor.getColumnIndex(NodeCache.NICK));
            }
            currentNode.setText(nodeNick);
            cursor.close();
        } catch (CursorIndexOutOfBoundsException e) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(messageReceiver);
    }


    public void showUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.update_content);
        builder.setTitle(R.string.update_title);
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences preferences = getActivity().getSharedPreferences("sakura", Context.MODE_PRIVATE);
                String apkName = preferences.getString("apk_name", "");
                Utilities.updateApp(getActivity(), DevicesUtilities.getUpdateFile(), apkName);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancle_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void showNewUpadteDialog() {
        UpdateAlertDialog dialog = new UpdateAlertDialog(getActivity());
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Button select = (Button) view.findViewById(R.id.select_btn);
        HashMap<String, String> map = (HashMap<String, String>) select.getTag();
        bindCdn(getActivity(), map.get("cdn_id"));
        TextView textView = new TextView(getActivity());
        textView.setTextSize(24);
        textView.setSingleLine(true);
        textView.setBackgroundColor(Color.BLACK);
        textView.setPadding(40,40,40,40);
        textView.setText(getActivity().getString(R.string.toast_1) + map.get("node") + getActivity().getString(R.string.toast_2));

        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(textView);
        toast.show();

    }

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MessageReceiver", "status message : " + intent.getIntExtra(Message.STATUS, -1));
            if (running == true)
                running = false;
            speedTestBtn.setText(R.string.test);
        }
    }


    private static void bindCdn(final Context context, final String cdn) {
        Observable.from(new String[]{"bindecdn"})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return CdnClient.bind(args, DevicesUtilities.getSNCode(), cdn);
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   Log.d(TAG, result);
                                   getCurrentCdn(context);
                               }
                           }
                );
    }


    public static void getCurrentCdn(final Context context) {
        Observable.from(new String[]{"getBindcdn"})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return CdnClient.get(args, DevicesUtilities.getSNCode());
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   Log.d(TAG, result);
                                   HttpData httpBindcdn = new Gson().fromJson(result, HttpData.class);
                                   CacheManager.updateCheck(context, httpBindcdn.getSncdn().getCdnid(), "true");
                               }
                           }
                );
    }


}
