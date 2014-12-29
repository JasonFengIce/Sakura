package cn.ismartv.speedtester.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.TeleEntity;
import cn.ismartv.speedtester.data.TicketEntity;
import cn.ismartv.speedtester.ui.activity.HomeActivity;
import cn.ismartv.speedtester.ui.activity.MenuActivity;
import cn.ismartv.speedtester.utils.DeviceUtils;
import org.w3c.dom.Text;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by huaijie on 14-10-29.
 */
public class FragmentHelp extends Fragment implements HomeActivity.OnBackPressListener {
    public static final int QR_CODE = 0x0001;
    private static final String TAG = "FragmentHelp";
    @InjectView(R.id.weixin_image)
    ImageView weixinImage;
    @InjectView(R.id.ismartv_title)
    TextView ismartvTitle;
    @InjectView(R.id.ismartv_tel)
    TextView ismartvTel;
    @InjectView(R.id.tv_title)
    TextView tvTitle;
    @InjectView(R.id.tv_tel)
    TextView tvTel;
    private Handler messageHandler;

    private Activity mActivity;

    private static void qrcode(String ticket, Handler handler) {
        if (AppConstant.DEBUG)
            Log.d(TAG, "ticket ---> " + ticket);

        try {
            URL url = new URL("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("GET");
            InputStream in = urlConnection.getInputStream();
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (null != handler) {
                Message message = handler.obtainMessage(QR_CODE, bmp);
                handler.sendMessage(message);
            }
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageHandler = new MessageHandler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_help, container, false);
        ButterKnife.inject(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTicket(mActivity);
        fetchTel();

    }

    private void getTicket(Context context) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.Ticket.HOST)
                .build();
        ClientApi.Ticket ticket = restAdapter.create(ClientApi.Ticket.class);
        ticket.excute(DeviceUtils.getLocalIpAddressV4(),
                DeviceUtils.getLocalMacAddress(context),
                new Callback<TicketEntity>() {
                    @Override
                    public void success(final TicketEntity ticketEntity, Response response) {
                        new Thread() {
                            @Override
                            public void run() {
                                qrcode(ticketEntity.getQrcode(), messageHandler);
                            }
                        }.start();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                }
        );
    }

    private void fetchTel() {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.Ticket.HOST)
                .build();
        ClientApi.FetchTel client = restAdapter.create(ClientApi.FetchTel.class);
        client.excute(ClientApi.FetchTel.ACTION,
                DeviceUtils.getModel(),
                DeviceUtils.getSnCode(),
                new Callback<List<TeleEntity>>() {
                    @Override
                    public void success(List<TeleEntity> teleEntities, Response response) {
                        ismartvTitle.setText(teleEntities.get(0).getTitle() + " : ");
                        ismartvTel.setText(teleEntities.get(0).getPhoneNo());
                        tvTitle.setText(teleEntities.get(1).getTitle() + " : ");
                        tvTel.setText(teleEntities.get(1).getPhoneNo());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                }
        );
    }

    @Override
    public void onBackPress() {
        Intent intent = new Intent(mActivity, MenuActivity.class);
        startActivity(intent);
    }


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QR_CODE:
                    break;
                default:
                    break;
            }
        }
    }
}
