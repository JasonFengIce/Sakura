package cn.ismartv.speedtester.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by fenghb on 14-7-2.
 */
public class DevicesUtilities {
    private static final String TAG = "DevicesUtilities";

    /**
     * if exist sdcard on android devices
     *
     * @return
     */
    public static boolean isExistSDCard() {


        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SDCard is available");
            return true;
        } else {
            Log.d(TAG, "SDCard is not available");
            return false;
        }
    }

    public static String getAppCacheDirectory() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sakura" +
                File.separator + "cache");
        if (!file.exists() && !file.isDirectory())
            file.mkdirs();
        return file.getAbsolutePath();
    }

    public static String getUpdateDirectory() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sakura" +
                File.separator + "update");
        if (!file.exists() && !file.isDirectory())
            file.mkdirs();
        Log.d(TAG, "update directory --> " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static File getUpdateFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sakura" +
                File.separator + "update");
        if (!file.exists() && !file.isDirectory())
            file.mkdirs();
        return file;
    }

    public static String getSNCode() {
        String fileContent = new String();
        try {
            String encoding = "UTF-8";
            File file = new File("/sn");
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    fileContent += lineTxt;
                }
                read.close();
            } else {
                if ("unknown".equals(Build.SERIAL)) {
                    return "0";
                } else {
                    return Build.SERIAL;
                }
            }
        } catch (IOException e) {
            return "0";
        }
        return fileContent;
    }

    public static String getLocalIpAddressV4() {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()))  //这里做了一步IPv4的判定
                    {
                        ip = inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
//            Log.i("SocketException--->", ""+e.getLocalizedMessage());
            return "ip is error";
        }
        Log.d(TAG, "ip address --> " + ip);
        return ip;
    }

    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String macAddress = info.getMacAddress();
        if (null == macAddress)
            macAddress = "0:0:0:0:0:0";
        return macAddress;
    }
}
