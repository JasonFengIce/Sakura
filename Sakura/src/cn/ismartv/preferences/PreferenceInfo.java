package cn.ismartv.preferences;

import android.text.TextUtils;
import cn.ismartv.preferences.annotation.PreferenceItem;
import cn.ismartv.preferences.annotation.Preference;
import cn.ismartv.preferences.util.PreferenceReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by huaijie on 12/10/14.
 */
public class PreferenceInfo {
    private Class<? extends PreferenceModel> mType;

    private String mPreferenceName;

    private Map<Field, String> mItemNames = new LinkedHashMap<Field, String>();

    public PreferenceInfo(Class<? extends PreferenceModel> type) {
        this.mType = type;

        final Preference preferenceAnnotation = type.getAnnotation(Preference.class);
        if (preferenceAnnotation != null) {
            mPreferenceName = preferenceAnnotation.name();
        } else {
            mPreferenceName = type.getSimpleName();
        }

        List<Field> fields = new LinkedList<Field>(PreferenceReflectionUtils.getDeclaredItemFields(type));
        Collections.reverse(fields);


        for (Field field : fields) {
            if (field.isAnnotationPresent(PreferenceItem.class)) {
                final PreferenceItem itemAnnotation = field.getAnnotation(PreferenceItem.class);
                String itemName = itemAnnotation.name();
                if (TextUtils.isEmpty(itemName)) {
                    itemName = field.getName();
                }

                mItemNames.put(field, itemName);
            }
        }

    }


    public Class<? extends PreferenceModel> getType() {
        return mType;
    }


    public String getPreferenceName() {
        return mPreferenceName;
    }


    public Collection<Field> getFields() {
        return mItemNames.keySet();
    }

    public String getItemName(Field field) {
        return mItemNames.get(field);
    }


}
