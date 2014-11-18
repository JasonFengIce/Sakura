package cn.ismartv.speedtester.preferences;

import android.content.Context;
import cn.ismartv.speedtester.preferences.serializer.TypeSerializer;
import cn.ismartv.speedtester.preferences.util.ReflectionUtils;
import com.activeandroid.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaijie on 11/18/14.
 */
public class Configuration {

    private List<Class<? extends Model>> mModelClasses;
    private List<Class<? extends TypeSerializer>> mTypeSerializers;

    public List<Class<? extends Model>> getmModelClasses() {
        return mModelClasses;
    }

    public List<Class<? extends TypeSerializer>> getTypeSerializers() {
        return mTypeSerializers;
    }

    public void setmTypeSerializers(List<Class<? extends TypeSerializer>> mTypeSerializers) {
        this.mTypeSerializers = mTypeSerializers;
    }

    private List<Class<? extends Model>> loadModelList(Context context, String[] models) {
        final List<Class<? extends Model>> modelClasses = new ArrayList<Class<? extends Model>>();
        final ClassLoader classLoader = context.getClass().getClassLoader();

        for (String model : models) {

            try {
                Class modelClass = Class.forName(model.trim(), false, classLoader);
                if (ReflectionUtils.isModel(modelClass))
                    modelClasses.add(modelClass);
            } catch (ClassNotFoundException e) {
                Log.e("Couldn't create class.", e);
            }
        }
        return modelClasses;
    }


    public boolean isValid() {
        return mModelClasses != null && mModelClasses.size() > 0;
    }

}
