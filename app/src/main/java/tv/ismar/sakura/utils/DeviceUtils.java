package tv.ismar.sakura.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;

/**
 * Created by huaijie on 14-10-30.
 */
public class DeviceUtils {


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
            return "0.0.0.0";
        }
        return ip;
    }


    public static String getModel() {
        return Build.MODEL;
    }

    public static String getMd5ByFile(File file) {
        String value;
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            BigInteger bi = new BigInteger(1, messageDigest.digest());
            value = bi.toString(16);
            in.close();
        } catch (Exception e) {
            Log.e("getMd5ByFile", e.getMessage());
            return "";
        }

        int offset = 32 - value.length();
        if (offset > 0) {
            String data = new String();
            for (int i = 0; i < offset; i++) {
                data = data + "0";
            }
            value = data + value;
        }
        return value;
    }

    public static String getAppCacheDirectory(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }


    public static String ipToHex() {

        String ipAddress = DeviceUtils.getLocalIpAddressV4();

        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = ipAddress.indexOf(".");
        int position2 = ipAddress.indexOf(".", position1 + 1);
        int position3 = ipAddress.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(ipAddress.substring(0, position1));
        ip[1] = Long.parseLong(ipAddress.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(ipAddress.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(ipAddress.substring(position3 + 1));
        long ipAddressLong = (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
        return Long.toHexString(ipAddressLong);
    }

    public static String getSnToken() {
        return "1";
    }

}
