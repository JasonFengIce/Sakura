package cn.ismartv.preferences.util;

import cn.ismartv.preferences.PreferenceModel;
import cn.ismartv.preferences.annotation.PreferenceItem;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by huaijie on 12/10/14.
 */
public class PreferenceReflectionUtils {

    public static boolean isModel(Class<?> type) {
        return isSubclassOf(type, PreferenceModel.class) && (!Modifier.isAbstract(type.getModifiers()));
    }

    /**
     * get item fields
     */
    public static Set<Field> getDeclaredItemFields(Class<?> type) {
        Set<Field> declaredColumnFields = Collections.emptySet();

        if (PreferenceReflectionUtils.isSubclassOf(type, PreferenceModel.class) || PreferenceModel.class.equals(type)) {
            declaredColumnFields = new LinkedHashSet<Field>();

            Field[] fields = type.getDeclaredFields();
            Arrays.sort(fields, new Comparator<Field>() {
                @Override
                public int compare(Field field1, Field field2) {
                    return field2.getName().compareTo(field1.getName());
                }
            });
            for (Field field : fields) {
                if (field.isAnnotationPresent(PreferenceItem.class)) {
                    declaredColumnFields.add(field);
                }
            }

            Class<?> parentType = type.getSuperclass();
            if (parentType != null) {
                declaredColumnFields.addAll(getDeclaredItemFields(parentType));
            }
        }

        return declaredColumnFields;
    }


    /**
     * assert type is sub Class
     */
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
