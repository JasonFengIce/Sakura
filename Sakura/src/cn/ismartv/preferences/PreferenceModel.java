package cn.ismartv.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cn.ismartv.preferences.annotation.PreferenceItem;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by huaijie on 12/10/14.
 */
public class PreferenceModel {
    private static final String TAG = "Model";

    private static PreferenceInfo mPreferenceInfo;
    private static Context mContext;

    public PreferenceModel() {
        mPreferenceInfo = PreferenceInitializer.getPreferenceInfo(getClass());
        mContext = PreferenceInitializer.getContext();
    }


    public void save() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mPreferenceInfo.getPreferenceName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Field field : mPreferenceInfo.getFields()) {
            final String fieldName = mPreferenceInfo.getItemName(field);
            Class<?> fieldType = field.getType();
            field.setAccessible(true);

            try {
                Object value = field.get(this);

                if (value == null) {
                    //----------
                } else if (fieldType.equals(String.class)) {
                    editor.putString(fieldName, value.toString());
                } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                    editor.putInt(fieldName, (Integer) value);
                } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                    editor.putBoolean(fieldName, (Boolean) value);
                } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                    editor.putFloat(fieldName, (Float) value);
                } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                    editor.putLong(fieldName, (Long) value);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage());
            }
            editor.apply();
        }
    }

    public static <T extends PreferenceModel> T load(Class<T> type) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mPreferenceInfo.getPreferenceName(), Context.MODE_PRIVATE);
        PreferenceInfo preferenceInfo = PreferenceInitializer.getPreferenceInfo(type);

        T sModel = null;
        try {
            sModel = type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Map<String, ?> map = sharedPreferences.getAll();
        for (Field field : sModel.getClass().getFields()) {
            String key = field.getAnnotation(PreferenceItem.class).name();
            try {
                field.set(sModel, map.get(key));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return sModel;
    }
}
