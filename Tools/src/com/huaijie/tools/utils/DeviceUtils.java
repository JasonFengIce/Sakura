package com.huaijie.tools.utils;

import android.os.Build;

/**
 * Created by <huaijiefeng@gmail.com> on 9/18/14.
 */
public class DeviceUtils {
    private static final String IDEATV_A21 = "ideatv A21";

    public static boolean isIdeatvA21() {
        if (IDEATV_A21.equals(Build.MODEL))
            return true;
        return false;
    }

    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }
}
