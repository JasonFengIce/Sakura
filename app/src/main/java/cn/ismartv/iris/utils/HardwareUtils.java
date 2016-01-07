package cn.ismartv.iris.utils;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by huaijie on 3/12/15.
 */
public class HardwareUtils {


    public static String getMd5ByString(String string) {
        String value;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            value = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception e) {
            Log.e("getMd5ByFile", e.getMessage());
            return "";
        }
        return value;
    }

}
