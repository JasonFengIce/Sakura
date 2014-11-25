package com.ismartv.android.vod.core.install;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.data.VersionInfoEntity;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by huaijie on 11/25/14.
 */
public class BootInstallTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "BootInstallTask";
    private static final String SELF_APP_NAME = "sakura.app";
    private static final String VOD_APP_NAME = "ismartv_vod_service_sign.apk";

    private static final int DEFAULT_VALUE = 1;
    private static final int MAX_CHECK_TIME = 3;

    private Context context;

    public BootInstallTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        getLatestAppVersion(context);
        installVodService(context);
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }


    private void installVodService(Context context) {
        if (AppConstant.DEBUG)
            Log.d(TAG, "install vod service invoke...");
        try {
            context.getPackageManager().getApplicationInfo(
                    "com.ismartv.android.vod.service", 0);
        } catch (PackageManager.NameNotFoundException e) {
            ///////////////////////////////////////////////////////////
            //If System No This Package, Then Install Vod Service Apk
            ///////////////////////////////////////////////////////////
            parseAsset(context);
        }
    }


    private void selfUpdate() {
        if (AppConstant.DEBUG)
            Log.d(TAG, "app self update invoke...");
        File file = new File(context.getFilesDir(), SELF_APP_NAME);
        if (file.exists()) {
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            Intent intent = new Intent("android.intent.action.VIEW.HIDE");
            intent.putExtra("com.lenovo.nebula.packageinstaller.INSTALL_EXTERNAL", false);
            intent.setDataAndType(uri,
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //Parse Asset Directory, Move Vod Service Apk To data/file, Then Install
    ////////////////////////////////////////////////////////////////////////
    private void parseAsset(Context context) {
        if (AppConstant.DEBUG)
            Log.d(TAG, "parse asset invoke...");
        try {
            InputStream inputStream = context.getAssets().open(VOD_APP_NAME);
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch;
            while ((ch = inputStream.read()) != -1) {
                bytestream.write(ch);
            }
            byte imgdata[] = bytestream.toByteArray();
            bytestream.close();
            File cacheDir = context.getFilesDir();
            File temfileName = new File(cacheDir.getAbsolutePath(), VOD_APP_NAME);
            if (!temfileName.exists())
                temfileName.createNewFile();
            FileOutputStream fout = context.openFileOutput(VOD_APP_NAME, Context.MODE_WORLD_READABLE);
            fout.write(imgdata);
            fout.flush();
            fout.close();
            Intent intent = new Intent("android.intent.action.VIEW.HIDE");
            intent.putExtra("com.lenovo.nebula.packageinstaller.INSTALL_EXTERNAL", false);
            Uri uri = Uri.parse("file://" + temfileName.getAbsolutePath());
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (IOException e) {
            Log.e(TAG, "parse assert exception");
        }
    }

    public void getLatestAppVersion(final Context context) {
        if (AppConstant.DEBUG)
            Log.d(TAG, "get latest app version invoke...");
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.AppVersionInfo client = restAdapter.create(ClientApi.AppVersionInfo.class);
        client.excute(ClientApi.AppVersionInfo.ACTION, new Callback<VersionInfoEntity>() {
            @Override
            public void success(VersionInfoEntity versionInfo, Response response) {
                CacheManager.updateSpeedLogUrl(context, versionInfo.getSpeedlogurl());
                PackageInfo packageInfo = null;
                try {
                    packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "No this Apk!!!");
                }
                if (packageInfo.versionCode < Integer.parseInt(versionInfo.getVersion())) {
                    if (AppConstant.DEBUG) {
                        Log.d(TAG, "local app version ---> " + packageInfo.versionCode);
                        Log.d(TAG, "server app verson ---> " + versionInfo.getVersion());
                    }
                    Toast.makeText(context, R.string.app_update_message, Toast.LENGTH_LONG).show();
                    ////////////////////////////////////////////////////////////////////////////////////
                    //If Local Version Less
                    ////////////////////////////////////////////////////////////////////////////////////
                    downloadAPK(context, versionInfo.getDownloadurl(), versionInfo.getMd5(), DEFAULT_VALUE);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "get Latest AppVersion exception!!!");
            }
        });
    }


    private void downloadAPK(final Context context, final String downloadUrl, final String md5, final int count) {
        new Thread() {
            @Override
            public void run() {

                if (count > MAX_CHECK_TIME)

                {
                    Log.d(TAG, "start download apk time --> " + count);
                    return;
                }

                File fileName = null;
                try

                {
                    int byteread;
                    URL url = new URL(downloadUrl);
                    fileName = new File(context.getFilesDir().getAbsolutePath(), SELF_APP_NAME);
                    if (!fileName.exists())
                        fileName.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = context.openFileOutput(SELF_APP_NAME, Context.MODE_WORLD_READABLE);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    fs.flush();
                    fs.close();
                    inStream.close();
                } catch (
                        MalformedURLException e
                        )

                {
                    e.printStackTrace();
                } catch (
                        IOException e
                        )

                {
                    e.printStackTrace();
                }

                String MD5Value = getMd5ByFile(fileName);
                if (!md5.equals(MD5Value))

                {
                    downloadAPK(context, downloadUrl, md5, count + 1);
                }

                selfUpdate();
            }

        }.start();
    }


    private static String getMd5ByFile(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
