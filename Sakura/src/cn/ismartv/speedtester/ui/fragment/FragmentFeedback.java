package cn.ismartv.speedtester.ui.fragment;

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
import cn.ismartv.speedtester.data.ChatMsgEntity;
import cn.ismartv.speedtester.data.FeedBackEntity;
import cn.ismartv.speedtester.data.ProblemEntity;
import cn.ismartv.speedtester.ui.adapter.FeedbackListAdapter;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.List;

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
        problemType.setOnCheckedChangeListener(this);
        SharedPreferences preferences = getActivity().getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        phone.setText(preferences.getString("feedback_phoneNumber", ""));

        fetchProblems();
        fetchFeedback(DeviceUtils.getSnCode(), "5");
        snCode.append(DeviceUtils.getSnCode());
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
                feedbackList.setAdapter(new FeedbackListAdapter(getActivity(), chatMsgEntities.getData()));
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    private void createProblemsRadio(List<ProblemEntity> problemEntities) {
        for (int i = 0; i < problemEntities.size(); i++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setTextSize(24);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 15, 0);
            radioButton.setLayoutParams(params);
            radioButton.setText(problemEntities.get(i).getPoint_name());
            radioButton.setId(problemEntities.get(i).getPoint_id());
            problemType.addView(radioButton);
        }
    }

    private void setFeedBack() {
        if (StringUtils.isEmpty(phone.getEditableText().toString()) || phone.getEditableText().toString().length() < 7) {
            Toast.makeText(getActivity(), R.string.you_should_give_an_phone_number, Toast.LENGTH_LONG).show();
            return;
        } else {
            FeedBackEntity feedBack = new FeedBackEntity();
            feedBack.setCity("");
            feedBack.setDescription(description.getEditableText().toString());
            feedBack.setIp("");
            feedBack.setPhone(phone.getEditableText().toString());
            feedBack.setIsp("");
            feedBack.setOption(problemText);
            feedBack.setWidth("");
            ClientApi.uploadFeedback(getActivity(), feedBack, messageHandler);
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
                    Toast.makeText(getActivity(), R.string.submit_sucess, Toast.LENGTH_LONG).show();
                    break;
                case UPLAOD_FEEDBACK_FAILED:
                    Toast.makeText(getActivity(), R.string.submit_failed, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    @OnClick(R.id.submit_btn)
    public void submitFeedback(View view) {
        CacheManager.updatFeedBack(getActivity(), phone.getText().toString());
        setFeedBack();
    }


    @OnClick({R.id.arrow_up, R.id.arrow_down})
    public void scrollList(View view) {

        switch (view.getId()) {
            case R.id.arrow_up:
                feedbackList.smoothScrollByOffset(-1);

                break;
            case R.id.arrow_down:
                feedbackList.smoothScrollByOffset(1);

                break;
            default:
                break;
        }
    }
}
