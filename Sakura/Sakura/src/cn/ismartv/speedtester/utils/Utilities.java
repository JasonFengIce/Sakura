package cn.ismartv.speedtester.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.*;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by fenghb on 14-7-11.
 */
public class Utilities {


    public static void showToast(Context context, int resId) {
        Toast toast = Toast.makeText(context, context.getResources().getString(resId), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static String getMd5ByFile(File file) {
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


    public static void updateApp(Context context, File path, String fileName) {

        Uri uri = Uri.parse("file://" + new File(path, fileName).getAbsolutePath());
        Intent intent = new Intent("android.intent.action.VIEW.HIDE");
        intent.putExtra("com.lenovo.nebula.packageinstaller.INSTALL_EXTERNAL", false);
        intent.setDataAndType(uri,
                "applicationnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


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


            Uri uri = Uri.parse("file://" + temfileName.getAbsolutePath());
            Intent intent = new Intent("android.intent.action.VIEW.HIDE");
            intent.putExtra("com.lenovo.nebula.packageinstaller.INSTALL_EXTERNAL", false);
            intent.setDataAndType(uri,
                    "applicationnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);


//            if (!temfileName.exists())
//                temfileName.createNewFile();
//            FileOutputStream fout = context.openFileOutput(
//                    "ismartv_vod_service_sign.apk", Context.MODE_WORLD_READABLE);
//            fout.write(imgdata);
//            fout.flush();
//            fout.close();
//
//            do_exec("adb connect 127.0.0.1");
//            do_exec("adb -s 127.0.0.1:5555 install -r /data/data/cn.ismartv.speedtester/files/http-proxy.apk");
//            // Uri uri = Uri.fromFile(temfileName);
//            // Intent intent = new Intent(Intent.ACTION_VIEW);
//            // intent.setDataAndType(uri,
//            // "application/vnd.android.package-archive");
//            // startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String do_exec(String cmd) {
        String s = "/n";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "/n";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.v("aaaa", e.getMessage());
            e.printStackTrace();
        }
        // text.setText(s);
        return cmd;
    }

    public static void installPackage(Context context) {
        try {
            context.getPackageManager().getApplicationInfo(
                    "com.ismartv.android.vod.service", 0);
        } catch (PackageManager.NameNotFoundException e) {
            parseAsset(context);
        }
    }
}
