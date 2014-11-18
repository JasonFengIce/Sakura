package cn.ismartv.speedtester.preferences.util;

import com.activeandroid.Model;

import java.lang.reflect.Modifier;

/**
 * Created by huaijie on 11/18/14.
 */
public final class ReflectionUtils {


    public static boolean isModel(Class<?> type) {
        return isSubclassOf(type, Model.class) && (!Modifier.isAbstract(type.getModifiers()));
    }

    public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {

        if (type.getSuperclass() != null) {
            if (type.getSuperclass().equals(superClass)) {
                return true;
            }
            return isSubclassOf(type.getSuperclass(), superClass);
        }
        return false;
    }
}
