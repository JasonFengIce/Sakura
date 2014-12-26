package cn.ismartv.speedtester.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.logger.Logger;
import cn.ismartv.speedtester.data.ChatMsgEntity;
import cn.ismartv.speedtester.data.FeedBackEntity;
import cn.ismartv.speedtester.data.ProblemEntity;
import cn.ismartv.speedtester.ui.adapter.FeedbackListAdapter;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.google.gson.Gson;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentFeedback extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "FragmentFeedback";

    public static final int UPLAOD_FEEDBACK_COMPLETE = 0x0001;
    public static final int UPLAOD_FEEDBACK_FAILED = 0x0002;

    @InjectView(R.id.sn_code)
    TextView snCode;
    RadioGroup problemType;
    @InjectView(R.id.feedback_list)
    ListView feedbackList;
    @InjectView(R.id.phone_number_edit)
    EditText phone;
    @InjectView(R.id.description_edit)
    EditText description;
    @InjectView(R.id.submit_btn)
    Button submitBtn;

    @InjectViews({R.id.arrow_up, R.id.arrow_down})
    List<ImageView> arrows;

    private int problemText = 6;
    private Handler messageHandler;

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
        View mView = inflater.inflate(R.layout.fragment_feedback, null);
        ButterKnife.inject(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        problemType = (RadioGroup) view.findViewById(R.id.problem_options);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        problemType.setOnCheckedChangeListener(this);
        SharedPreferences preferences = mActivity.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        phone.setText(preferences.getString("feedback_phoneNumber", ""));

        fetchProblems();
        fetchFeedback(DeviceUtils.getSnCode(), "10");
        snCode.append(DeviceUtils.getSnCode());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    private void fetchProblems() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .setEndpoint(ClientApi.Problems.HOST)
                .build();
        ClientApi.Problems client = restAdapter.create(ClientApi.Problems.class);
        client.excute(new Callback<List<ProblemEntity>>() {
            @Override
            public void success(List<ProblemEntity> problemEntities, retrofit.client.Response response) {
                createProblemsRadio(problemEntities);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private void fetchFeedback(String sn, String top) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.Feedback.HOST)
                .build();
        ClientApi.Feedback client = restAdapter.create(ClientApi.Feedback.class);
        client.excute(sn, top, new Callback<ChatMsgEntity>() {
            @Override
            public void success(ChatMsgEntity chatMsgEntities, Response response) {
                feedbackList.setAdapter(new FeedbackListAdapter(mActivity, chatMsgEntities.getData()));
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    private void createProblemsRadio(List<ProblemEntity> problemEntities) {
        RadioButton mRadioButton = null;
        if (null != mActivity) {
            for (int i = 0; i < problemEntities.size(); i++) {
                RadioButton radioButton = new RadioButton(mActivity);
                radioButton.setTextSize(24);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 15, 0);
                radioButton.setLayoutParams(params);
                radioButton.setText(problemEntities.get(i).getPoint_name());
                radioButton.setTextSize(30);
                radioButton.setId(problemEntities.get(i).getPoint_id());
                if (i == 0)
                    mRadioButton = radioButton;
                problemType.addView(radioButton);
            }

            mRadioButton.setChecked(true);
        }
    }

    private void setFeedBack() {
        String contactNumber = phone.getEditableText().toString();
        if (StringUtils.isEmpty(contactNumber)) {
            Toast.makeText(mActivity, R.string.fill_contact_number, Toast.LENGTH_LONG).show();
            return;
        } else if ((!isMobile(contactNumber) && !isPhone(contactNumber))) {
            Toast.makeText(mActivity, R.string.you_should_give_an_phone_number, Toast.LENGTH_LONG).show();
            return;
        } else {
            CacheManager.IpLookUp ipLookUp = CacheManager.getInstance(mActivity).new IpLookUp();
            FeedBackEntity feedBack = new FeedBackEntity();
            feedBack.setDescription(description.getEditableText().toString());
            feedBack.setPhone(phone.getEditableText().toString());
            feedBack.setOption(problemText);
            feedBack.setCity(ipLookUp.getUserCity());
            feedBack.setIp(ipLookUp.getUserIp());
            feedBack.setIsp(ipLookUp.getUserIsp());
            feedBack.setLocation(ipLookUp.getUserProvince());
            uploadFeedback(mActivity, feedBack, messageHandler);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        Log.d(TAG, "radioGroup position is ---> " + i);
        problemText = i;


    }


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLAOD_FEEDBACK_COMPLETE:
                    fetchFeedback(DeviceUtils.getSnCode(), "10");
                    if (null != mActivity)
                        Toast.makeText(mActivity, R.string.submit_sucess, Toast.LENGTH_LONG).show();
                    break;
                case UPLAOD_FEEDBACK_FAILED:
                    if (null != mActivity)
                        Toast.makeText(mActivity, R.string.submit_failed, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    @OnClick(R.id.submit_btn)
    public void submitFeedback(View view) {
        if (AppConstant.DEBUG)
            Log.d(TAG, "submit problem feedback");
        CacheManager.updatFeedBack(mActivity, phone.getText().toString());
        setFeedBack();
    }


    @OnClick({R.id.arrow_up, R.id.arrow_down})
    public void scrollList(View view) {

        switch (view.getId()) {
            case R.id.arrow_up:
//                feedbackList.smoothScrollByOffset(-1);
                feedbackList.smoothScrollBy(-100, 1);
                break;
            case R.id.arrow_down:
//                feedbackList.smoothScrollByOffset(1);
                feedbackList.smoothScrollBy(100, 1);
                break;
            default:
                break;
        }
    }

    public void uploadFeedback(Context context, FeedBackEntity feedBack, final Handler handler) {
        Log.d(TAG, "result is ---> " + "run");
        final String str = new Gson().toJson(feedBack);
        Log.d(TAG, "json is ---> " + str);
        new Thread() {
            @Override
            public void run() {
                com.activeandroid.util.Log.d(TAG, "result is ---> " + "run");
                String localeName = Locale.getDefault().toString();
                URL url;
                HttpURLConnection conn = null;
                InputStream inputStream;
                BufferedReader reader = null;
                StringBuffer sb = new StringBuffer();
                OutputStream outputStream;
                BufferedWriter writer = null;
                try {
                    url = new URL("http://iris.tvxio.com/customer/pointlogs/");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("content-type", "text/json");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", android.os.Build.MODEL.replaceAll(" ", "_") + "/" + android.os.Build.ID + " " + DeviceUtils.getSnCode());
                    conn.setRequestProperty("Accept-Language", localeName);
                    outputStream = conn.getOutputStream();
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write("q=" + str);
                    writer.flush();
                    int statusCode = conn.getResponseCode();
                    if (statusCode == 200) {
                        inputStream = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }

                        Logger logger = new Logger.Builder()
                                .setLevel(Logger.D)
                                .setMessage(sb.toString())
                                .setTag(TAG)
                                .build();
                        logger.log();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if ("OK".equals(sb.toString()) && null != handler) {
                    Log.d(TAG, "result is ---> " + sb.toString());
                    handler.sendEmptyMessage(FragmentFeedback.UPLAOD_FEEDBACK_COMPLETE);
                } else {
                    handler.sendEmptyMessage(FragmentFeedback.UPLAOD_FEEDBACK_FAILED);
                }
            }
        }.start();
    }

    /**
     * 手机号验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 电话号码验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isPhone(String str) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        p1 = Pattern.compile("^[0][0-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的
        if (str.length() > 9) {
            m = p1.matcher(str);
            b = m.matches();
        } else {
            m = p2.matcher(str);
            b = m.matches();
        }
        return b;
    }
}
