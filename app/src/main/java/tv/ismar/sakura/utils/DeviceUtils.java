package tv.ismar.sakura.utils;

import android.content.Context;
import android.os.Build;
import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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


}
