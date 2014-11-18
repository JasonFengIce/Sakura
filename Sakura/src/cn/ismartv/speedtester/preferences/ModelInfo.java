package cn.ismartv.speedtester.preferences;


import android.content.Context;
import cn.ismartv.speedtester.preferences.serializer.*;
import dalvik.system.DexFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.*;

/**
 * Created by huaijie on 11/18/14.
 */
public final class ModelInfo {


    private Map<Class<? extends Model>, PreferenceInfo> mPreferenceInfos = new HashMap<Class<? extends Model>, PreferenceInfo>();


    private Map<Class<?>, TypeSerializer> mTypeSerializers = new HashMap<Class<?>, TypeSerializer>() {
        {
            put(Calendar.class, new CalendarSerializer());
            put(java.sql.Date.class, new SqlDateSerializer());
            put(java.util.Date.class, new UtilDateSerializer());
            put(java.io.File.class, new FileSerializer());
        }
    };

//
//    public ModelInfo(Configuration configuration) {
//        if (!loadModelFromMetaData(configuration)) {
//            try {
//
//            }
//        }
//    }

    public PreferenceInfo getPreferenceInfo(Class<? extends Model> type) {
        return mPreferenceInfos.get(type);
    }


//    private boolean loadModelFromMetaData(Configuration configuration) {
//        if (!configuration.isValid())
//            return false;
//
//        final List<Class<? extends Model>> models = configuration.getmModelClasses();
//
//        if (models != null) {
//            for (Class<? extends Model> model : models) {
//                mPreferenceInfos.put(model, new PreferenceInfo(model));
//            }
//
//        }
//
//        final List<Class<? extends TypeSerializer>> typeSerializers = configuration.getTypeSerializers();
//
//        if (typeSerializers != null) {
//            for (Class<? extends TypeSerializer> typeSerializer : typeSerializers) {
//                try {
//                    TypeSerializer instance = typeSerializer.newInstance();
//                    m
//                }
//            }
//
//
//        }
//
//
//    }


    private void scanForModel(Context context) throws IOException {
        String packageName = context.getPackageName();
        String sourcePath = context.getApplicationInfo().sourceDir;
        List<String> paths = new ArrayList<String>();

        if (sourcePath != null && !(new File(sourcePath).isDirectory())){
            DexFile dexFile = new DexFile(sourcePath);
            Enumeration<String> entries = dexFile.entries();

            while (entries.hasMoreElements()){
                paths.add(entries.nextElement());
            }
        }else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()){
                String path = resources.nextElement().getFile();

                if (path.contains("bin")|| path.contains("classes")){
                    paths.add(path);
                }
            }
        }




    }


}
