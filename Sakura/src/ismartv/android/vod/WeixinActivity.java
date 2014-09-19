package ismartv.android.vod;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import com.huaijie.tools.utils.DeviceUtils;
import ismartv.android.vod.data.TicketEntity;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by <huaijiefeng@gmail.com> on 9/18/14.
 */
public class WeixinActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "WeixinActivity";
    public static final int QR_CODE = 0x0001;

    private ImageView weixinQRCode;
    private TextView deviceCode;
    private Button exitButton;

    public static Handler messageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageHandler = new MessageHandler();
        setContentView(R.layout.activity_weixin);
        initViews();
        getTicket(this);
    }

    private void initViews() {
        weixinQRCode = (ImageView) findViewById(R.id.weixin_qr_code);
        deviceCode = (TextView) findViewById(R.id.device_code);
        deviceCode.setText(Long.toHexString(DeviceUtils.ipToLong(DevicesUtilities.getLocalIpAddressV4())));
        exitButton = (Button) findViewById(R.id.weixin_exit);
        exitButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        onBackPressed();
    }


    interface Ticket {
        public static final String HOST = "http://wx.api.tvxio.com";

        @GET("/weixin4server/qrcodeaction")
        void excute(
                @Query("ipaddress") String ipaddress,
                @Query("macaddress") String macaddress,
                Callback<TicketEntity> callback
        );
    }

    private void getTicket(Context context) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Ticket.HOST)
                .build();
        Ticket ticket = restAdapter.create(Ticket.class);
        ticket.excute(DevicesUtilities.getLocalIpAddressV4(),
                DevicesUtilities.getLocalMacAddress(context),
                new Callback<TicketEntity>() {
                    @Override
                    public void success(final TicketEntity ticketEntity, Response response) {
                        new Thread() {
                            @Override
                            public void run() {
                                qrcode(ticketEntity.getQrcode());
                            }
                        }.start();
//                        fetchQrCode(ticketEntity.getQrcode());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {

                    }
                }
        );
    }

    interface QRCode {
        public static final String HOST = "https://mp.weixin.qq.com/";

        @GET("/cgi-bin/showqrcode")
        void excute(
                @Query("ticket") String ticket,
                Callback<byte[]> callback
        );
    }

    private void fetchQrCode(String ticket) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(QRCode.HOST)
                .build();
        QRCode qrCode = restAdapter.create(QRCode.class);
        qrCode.excute(ticket, new Callback<byte[]>() {
            @Override
            public void success(byte[] bytes, Response response) {
                Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
                weixinQRCode.setImageBitmap(bmp);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private static void qrcode(String ticket) {
        try {
            URL url = new URL("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket);
            Log.d(TAG, url.toString());
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("GET");
            InputStream in = urlConnection.getInputStream();
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (null != WeixinActivity.messageHandler) {
                Message message = WeixinActivity.messageHandler.obtainMessage(WeixinActivity.QR_CODE, bmp);
                WeixinActivity.messageHandler.sendMessage(message);
            }
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QR_CODE:
                    weixinQRCode.setImageBitmap((Bitmap) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}
