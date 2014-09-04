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
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.httpclient.NetWorkUtilities;
import cn.ismartv.speedtester.data.ChatMsgEntity;
import cn.ismartv.speedtester.data.Comment;
import cn.ismartv.speedtester.data.FeedBack;
import cn.ismartv.speedtester.ui.adapter.ChatMsgViewAdapter;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.StringUtilities;
import cn.ismartv.speedtester.utils.Utilities;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fenghb on 14-6-25.
 */
public class FeedbackFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "FeedbackFragment";
    public static final int UPLAOD_FEEDBACK_COMPLETE = 0x0001;
    public static final int UPLAOD_FEEDBACK_FAILED = 0x0002;

    private EditText description;
    private EditText phone;
    private RadioGroup problemType;

    private Button submit;
    private Button exit;

    private TextView snCode;

    //spinner
    private Spinner provinceSpinner;
    private Spinner netTypeSpinner;
    private Spinner netWidthSpinner;

    public static Handler messageHandler;

    //String
    private String problemText;
    private String provinceText;
    private String netTypeText;
    private String netWidthText;

    private RequestQueue mQueue;

    //view
    private ListView commentListView;

    //change view
    private View feedbackSubmitView;

    //change button
    private LinearLayout itemSubmitButton;
    private LinearLayout itemReplyButton;

    //commentView
    private TextView messageContent;


    private TextView submitQuestion;
    private TextView replyQuestion;

    private ImageView submitQuestionSelect;
    private ImageView replyQuestionSelect;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
        messageHandler = new MessageHandler();

        getComment();


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, null);
        //provinceSpinner
        provinceSpinner = (Spinner) view.findViewById(R.id.province_spinner);
        ArrayAdapter<CharSequence> provinceSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.citys, android.R.layout.simple_spinner_item);
        provinceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceSpinnerAdapter);
        provinceSpinner.setOnItemSelectedListener(this);
        //netTypeSpinner
        netTypeSpinner = (Spinner) view.findViewById(R.id.net_type_spinner);
        ArrayAdapter<CharSequence> netTypeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.operators, android.R.layout.simple_spinner_item);
        netTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        netTypeSpinner.setAdapter(netTypeSpinnerAdapter);
        netTypeSpinner.setOnItemSelectedListener(this);

        //netWidthSpinner
        netWidthSpinner = (Spinner) view.findViewById(R.id.net_width_spinner);

        ArrayAdapter<CharSequence> netWidthSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.net_width, android.R.layout.simple_spinner_item);
        netWidthSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        netWidthSpinner.setAdapter(netWidthSpinnerAdapter);
        netWidthSpinner.setOnItemSelectedListener(this);

        //comment list view
        commentListView = (ListView) view.findViewById(R.id.feedback_reply_layout);

        feedbackSubmitView = view.findViewById(R.id.feedback_submit_layout);

        itemSubmitButton = (LinearLayout) view.findViewById(R.id.feedback_submit);
        itemReplyButton = (LinearLayout) view.findViewById(R.id.reply);
        itemReplyButton.setOnClickListener(this);
        itemSubmitButton.setOnClickListener(this);


        //sn code
        snCode = (TextView) view.findViewById(R.id.sn_show);
        String sn = DevicesUtilities.getSNCode();
        if ("1".equals(sn)) {
            snCode.append(sn + getString(R.string.factory_device));
        } else {
            snCode.append(sn);
        }
        //radio group
        problemType = (RadioGroup) view.findViewById(R.id.problem_options);
        problemType.setOnCheckedChangeListener(this);

        //description
        description = (EditText) view.findViewById(R.id.description_edit);

        //phone number
        phone = (EditText) view.findViewById(R.id.phone_number_edit);

        //submit button
        submit = (Button) view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        //exit button
        exit = (Button) view.findViewById(R.id.exit);
        exit.setOnClickListener(this);

        //default value
        problemText = getString(R.string.radiobutton_others);

        messageContent = (TextView) view.findViewById(R.id.content);

        submitQuestion = (TextView) view.findViewById(R.id.submit_question_tv);

        submitQuestion.setText("提\n交\n问\n题");
        replyQuestion = (TextView) view.findViewById(R.id.reply_question_tv);
        replyQuestion.setText("客\n服\n回\n复");

        submitQuestionSelect = (ImageView) view.findViewById(R.id.submit_question_select);
        replyQuestionSelect = (ImageView) view.findViewById(R.id.reply_question_select);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        provinceSpinner.setSelection(getProvinceByCache());
//        netTypeSpinner.setSelection(getNetTypeByCache());
//        netWidthSpinner.setSelection(getNetWidthByCache());
//        phone.setText(getPhoneNumberCache());


    }

    private void setFeedBack() {
        if (StringUtilities.isEmpty(phone.getEditableText().toString())) {
            Utilities.showToast(getActivity(), R.string.you_should_give_an_phone_number);
            return;
        } else if (StringUtilities.isEmpty(provinceText)) {
            Utilities.showToast(getActivity(), R.string.please_give_a_valid_address);
            return;
        } else {
            FeedBack feedBack = new FeedBack();
            feedBack.setCity(provinceText);
            feedBack.setDescriptionl(description.getEditableText().toString());
            feedBack.setIp("  ");
            feedBack.setPhone(phone.getEditableText().toString());
            feedBack.setIsp(netTypeText);
            feedBack.setLocation("    ");
            feedBack.setMail("    ");
            feedBack.setOption(problemText);
            feedBack.setIs_correct("true");
            feedBack.setSpeed(null);
            CacheManager.updateFeedBack(getActivity(), provinceText, netTypeText, netWidthText, phone.getEditableText().toString());
            NetWorkUtilities.uploadFeedback(getActivity(), feedBack);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                setFeedBack();
                break;
            case R.id.exit:
                getActivity().finish();
                break;
            case R.id.reply:
                commentListView.setVisibility(View.VISIBLE);
                feedbackSubmitView.setVisibility(View.INVISIBLE);
                replyQuestionSelect.setVisibility(View.VISIBLE);
                submitQuestionSelect.setVisibility(View.INVISIBLE);
                break;
            case R.id.feedback_submit:
                commentListView.setVisibility(View.INVISIBLE);
                feedbackSubmitView.setVisibility(View.VISIBLE);
                replyQuestionSelect.setVisibility(View.INVISIBLE);
                submitQuestionSelect.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if (id == R.id.problem_block)
            problemText = getString(R.string.radiobutton_block);
        else if (id == R.id.unable_play)
            problemText = getString(R.string.radiobutton_unable_play);
        else if (id == R.id.problem_unclear)
            problemText = getString(R.string.radiobutton_unclear);
        else if (id == R.id.problem_other)
            problemText = getString(R.string.radiobutton_others);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        switch (adapterView.getId()) {
            case R.id.province_spinner:
                provinceText = getResources().getStringArray(R.array.citys)[position];
                break;
            case R.id.net_type_spinner:
                netTypeText = getResources().getStringArray(R.array.operators)[position];
                break;
            case R.id.net_width_spinner:
                netWidthText = getResources().getStringArray(R.array.net_width)[position];
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private int getProvinceByCache() {
        SharedPreferences preferences = getActivity().getSharedPreferences("sakura", Context.MODE_PRIVATE);
        String[] provinces = getActivity().getResources().getStringArray(R.array.citys);
        for (int i = 0; i < provinces.length; i++) {
            if (provinces[i].equals(preferences.getString("feedback_province", "")))
                return i;
        }
        return 0;
    }

    private int getNetTypeByCache() {
        SharedPreferences preferences = getActivity().getSharedPreferences("sakura", Context.MODE_PRIVATE);
        String[] provinces = getActivity().getResources().getStringArray(R.array.operators);
        for (int i = 0; i < provinces.length; i++) {
            if (provinces[i].equals(preferences.getString("feedback_netType", "")))
                return i;
        }
        return 0;
    }

    private int getNetWidthByCache() {
        SharedPreferences preferences = getActivity().getSharedPreferences("sakura", Context.MODE_PRIVATE);
        String[] provinces = getActivity().getResources().getStringArray(R.array.net_width);
        for (int i = 0; i < provinces.length; i++) {
            if (provinces[i].equals(preferences.getString("feedback_netWidth", "")))
                return i;
        }
        return 0;
    }

    private String getPhoneNumberCache() {
        SharedPreferences preferences = getActivity().getSharedPreferences("sakura", Context.MODE_PRIVATE);
        return preferences.getString("feedback_phoneNumber", "");
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLAOD_FEEDBACK_COMPLETE:
                    Utilities.showToast(getActivity(), R.string.submit_sucess);
                    break;
                case UPLAOD_FEEDBACK_FAILED:
                    Utilities.showToast(getActivity(), R.string.submit_failed);
                    break;
                default:
                    break;
            }

        }
    }

    public void getComment() {

        StringRequest stringRequest = new StringRequest("http://iris.tvxio.com/customer/getfeedback/?sn=7f0622cf&topn=5",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        Comment comment = new Gson().fromJson(response, (Comment.class));

//                        messageContent.setText(comment.getData().get(0).getSubmit_time() + " : " + comment.getData().get(0).getCommont());
                        List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
                        for (Comment.Data data : comment.getData()) {
                            ChatMsgEntity send = new ChatMsgEntity();
                            ChatMsgEntity reply = new ChatMsgEntity();
                            send.setDate(data.getSubmit_time());
                            send.setMsgType(true);
                            send.setName("me");
                            send.setText(data.getSubmit_time() + "  " + data.getCommont());

                            reply.setDate(data.getReply_time());
                            reply.setMsgType(false);
                            reply.setName("ismartv");
                            reply.setText(data.getReply_time() + " " + data.getReply());

                            mDataArrays.add(send);
                            mDataArrays.add(reply);
                        }
                        ChatMsgViewAdapter mAdapter = new ChatMsgViewAdapter(getActivity(), mDataArrays);
                        commentListView.setAdapter(mAdapter);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                });

        mQueue.add(stringRequest);
    }


}

