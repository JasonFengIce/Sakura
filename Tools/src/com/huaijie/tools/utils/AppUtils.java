package com.huaijie.tools.utils;

import android.content.Context;
import android.util.Log;

import java.io.*;

/**
 * Created by <huaijiefeng@gmail.com> on 9/18/14.
 */
public class AppUtils {

    public static void parseAsset(Context context) {
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
            Log.v("aaaa", temfileName.getAbsolutePath());
            if (!temfileName.exists())
                temfileName.createNewFile();
            FileOutputStream fout = context.openFileOutput(
                    "ismartv_vod_service_sign.apk", Context.MODE_WORLD_READABLE);
            fout.write(imgdata);
            fout.flush();
            fout.close();
//
//            do_exec("adb connect 127.0.0.1");
//            do_exec("adb -s 127.0.0.1:5555 install -r /data/data/cn.ismartv.speedtester/files/http-proxy.apk");
//            // Uri uri = Uri.fromFile(temfileName);
            // Intent intent = new Intent(Intent.ACTION_VIEW);
            // intent.setDataAndType(uri,
            // "application/vnd.android.package-archive");
            // startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void installAppByShell() {


    }

    public static byte[] fetchAssertByByte(Context context, String assetName) {
        ByteArrayOutputStream bytestream = null;
        try {
            InputStream inputStream = context.getAssets().open(assetName);
            bytestream = new ByteArrayOutputStream();
            int buffer;
            while ((buffer = inputStream.read()) != -1) {
                bytestream.write(buffer);
                bytestream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytestream.toByteArray();
    }
}
