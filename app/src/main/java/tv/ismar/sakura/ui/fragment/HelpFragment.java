package tv.ismar.sakura.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tv.ismar.sakura.R;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.core.SimpleRestClient;
import tv.ismar.sakura.data.http.TeleEntity;
import tv.ismar.sakura.utils.DeviceUtils;
import retrofit2.Callback;
import retrofit2.Response;

import static tv.ismar.sakura.core.SakuraClientAPI.restAdapter_WX_API_TVXIO;

/**
 * Created by huaijie on 2015/4/8.
 */
public class HelpFragment extends Fragment {
    private static final String TAG = "HelpFragment";

    private String snCode = TextUtils.isEmpty(tv.ismar.sakura.core.SimpleRestClient.sn_token) ? "sn is null" : tv.ismar.sakura.core.SimpleRestClient.sn_token;

    private TextView ismartvTitle;
    private TextView ismartvTel;
    private TextView tvTitle;
    private TextView tvTel;
    private TextView deviceCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_help, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ismartvTitle = (TextView) view.findViewById(R.id.ismartv_title);
        ismartvTel = (TextView) view.findViewById(R.id.ismartv_tel);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTel = (TextView) view.findViewById(R.id.tv_tel);
        deviceCode = (TextView) view.findViewById(R.id.device_code);
        deviceCode.setText(" " + tv.ismar.sakura.utils.DeviceUtils.ipToHex());

        fetchTel(Build.MODEL, snCode);

    }

    private void fetchTel(String model, String snCode) {
        tv.ismar.sakura.core.SakuraClientAPI.FetchTel client = restAdapter_WX_API_TVXIO.create(tv.ismar.sakura.core.SakuraClientAPI.FetchTel.class);
        client.excute(tv.ismar.sakura.core.SakuraClientAPI.FetchTel.ACTION, model, snCode).enqueue(new Callback<List<tv.ismar.sakura.data.http.TeleEntity>>() {
            @Override
            public void onResponse(Response<List<tv.ismar.sakura.data.http.TeleEntity>> response) {
                List<tv.ismar.sakura.data.http.TeleEntity> teleEntities = response.body();
                ismartvTitle.setText(teleEntities.get(0).getTitle() + " : ");
                ismartvTel.setText(teleEntities.get(0).getPhoneNo());
                tvTitle.setText(teleEntities.get(1).getTitle() + " : ");
                tvTel.setText(teleEntities.get(1).getPhoneNo());
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "fetchTel: error");
            }
        });
    }

}
