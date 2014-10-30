package cn.ismartv.speedtester.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.data.ProblemEntity;
import cn.ismartv.speedtester.utils.DeviceUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import java.util.List;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentFeedback extends Fragment {
    @InjectView(R.id.sn_code)
    TextView snCode;
    @InjectView(R.id.problem_options)
    RadioGroup problemType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_feedback, container, false);
        ButterKnife.inject(this, mView);
        return mView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fetchProblems();
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

    private void createProblemsRadio(List<ProblemEntity> problemEntities) {
        for (int i = 0; i < problemEntities.size(); i++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setTextSize(24);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 15, 0);
            radioButton.setLayoutParams(params);
            radioButton.setText(problemEntities.get(i).getPoint_name());
            radioButton.setTag(problemEntities.get(i).getPoint_id());
            problemType.addView(radioButton);
        }
    }
}
