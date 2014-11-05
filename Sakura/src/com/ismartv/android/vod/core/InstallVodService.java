package com.ismartv.android.vod.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.logger.Logger;
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
 * Created by huaijie on 14-11-4.
 */
public class InstallVodService {
    private static final String TAG = "InstallVodService";

    private static final int DEFAULT_VALUE = 1;

    private static final int MAX_CHECK_TIME = 3;

    public static void install(Context context) {
        selfUpdate(context);
        installVodService(context);
    }


    private static void selfUpdate(Context context) {


        File file = new File(context.getFilesDir(), AppConstant.APP_NAME + ".apk");
        if (file.exists()) {
            Logger logger = new Logger.Builder()
                    .setLevel(Logger.I)
                    .setMessage("self update file, path is ---> " + file.getAbsolutePath())
                    .setTag(TAG)
                    .build();
            logger.log();
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            Intent intent = new Intent("android.intent.action.VIEW.HIDE");
            intent.putExtra("com.lenovo.nebula.packageinstaller.INSTALL_EXTERNAL", false);
            intent.setDataAndType(uri,
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    private static void parseAsset(Context context) {
        try {
            InputStream inputStream = context.getAssets().open(
                    "ismartv_vod_service_sign.apk");
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch;
            while ((ch = inputStream.read()) != -1) {
                bytestream.write(ch);
            }
            byte imgdata[] = bytestream.toByteArray();
            bytestream.close();
            File cacheDir = context.getFilesDir();
            File temfileName = new File(cacheDir.getAbsolutePath(),
                    "ismartv_vod_service_sign.apk");
            if (!temfileName.exists())
                temfileName.createNewFile();
            FileOutputStream fout = context
                    .openFileOutput("ismartv_vod_service_sign.apk",
                            Context.MODE_WORLD_READABLE);
            fout.write(imgdata);
            fout.flush();
            fout.close();
            Intent intent = new Intent("android.intent.action.VIEW.HIDE");
            intent.putExtra(
                    "com.lenovo.nebula.packageinstaller.INSTALL_EXTERNAL",
                    false);
            Uri uri = Uri.parse("file://" + temfileName.getAbsolutePath());
            intent.setDataAndType(uri,
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void installVodService(Context context) {
        try {
            context.getPackageManager().getApplicationInfo(
                    "com.ismartv.android.vod.service", 0);
        } catch (PackageManager.NameNotFoundException e) {
            parseAsset(context);
        }
    }


    public static void getLatestAppVersion(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.AppVersionInfo client = restAdapter.create(ClientApi.AppVersionInfo.class);
        client.excute("getLatestAppVersion", new Callback<VersionInfoEntity>() {
            @Override
            public void success(VersionInfoEntity versionInfo, Response response) {
                CacheManager.updateSpeedLogUrl(context, versionInfo.getSpeedlogurl());

                PackageInfo packageInfo = null;

                try {
                    packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (packageInfo.versionCode < Integer.parseInt(versionInfo.getVersion())) {
                    Logger logger = new Logger.Builder()
                            .setLevel(Logger.I)
                            .setMessage("find app update, server version code is ---> " + versionInfo.getVersion())
                            .setMessage("local version code is ---> " + packageInfo.versionCode)
                            .setTag(TAG)
                            .build();
                    logger.log();
                    if (AppConstant.DEBUG)
                        Log.d(TAG, "server version code --> " + versionInfo.getVersion() + " local version code --> " + packageInfo.versionCode);
                    downloadAPK(context, versionInfo.getDownloadurl(), versionInfo.getMd5(), DEFAULT_VALUE);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private static void downloadAPK(final Context context, final String downloadUrl, final String md5, final int count) {
        new Thread() {
            @Override
            public void run() {
                if (count > MAX_CHECK_TIME) {
                    Log.d(TAG, "start download apk time --> " + count);
                    return;
                }
                File fileName = null;
                try {
                    int byteread;
                    URL url = new URL(downloadUrl);
                    fileName = new File(
                            context.getFilesDir().getAbsolutePath(),
                            AppConstant.APP_NAME + ".apk");
                    if (!fileName.exists())
                        fileName.createNewFile();
                    URLConnection conn = url.openConnection();
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = context.openFileOutput(
                            AppConstant.APP_NAME + ".apk", Context.MODE_WORLD_READABLE);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, byteread);
                    }
                    fs.flush();
                    fs.close();
                    inStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String MD5Value = getMd5ByFile(fileName);
                if (!md5.equals(MD5Value)) {
                    downloadAPK(context, downloadUrl, md5, count + 1);
                }
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
