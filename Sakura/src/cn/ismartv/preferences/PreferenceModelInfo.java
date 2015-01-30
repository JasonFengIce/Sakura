package cn.ismartv.preferences;

import android.content.Context;
import android.util.Log;
import cn.ismartv.preferences.util.PreferenceReflectionUtils;
import dalvik.system.DexFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by huaijie on 12/10/14.
 */
public final class PreferenceModelInfo {
    private static final String TAG = "ModelInfo";

    private Map<Class<? extends PreferenceModel>, PreferenceInfo> mPreferences = new HashMap<Class<? extends PreferenceModel>, PreferenceInfo>();


    public PreferenceModelInfo(Context context) {
        try {
            scanForModel(context);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * get preferences info
     */

    public PreferenceInfo getPreferenceInfo(Class<? extends PreferenceModel> type) {
        return mPreferences.get(type);
    }


    private void scanForModel(Context context) throws IOException {
        String packageName = context.getPackageName();
        String sourcePath = context.getApplicationInfo().sourceDir;

        List<String> paths = new ArrayList<String>();


        if (sourcePath != null && !(new File(sourcePath).isDirectory())) {
            if (PreferenceInitializer.DEBUG)
                Log.d(TAG, "scan for model [ Dex ... ]");

            DexFile dexFile = new DexFile(sourcePath);
            Enumeration<String> entries = dexFile.entries();

            while (entries.hasMoreElements()) {
                paths.add(entries.nextElement());
            }
        } else {
            if (PreferenceInitializer.DEBUG)
                Log.d(TAG, "scan for model [ ClassLoader ...]");
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                String path = resources.nextElement().getFile();
                if (path.contains("bin") || path.contains("classes")) {
                    paths.add(path);
                }
            }

        }

        for (String path : paths) {
            File file = new File(path);
            scanForModelClasses(file, packageName, context.getClassLoader());
        }

    }


    /**
     * -----
     */
    private void scanForModelClasses(File path, String packageName, ClassLoader classLoader) {
        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                scanForModelClasses(file, packageName, classLoader);
            }
        } else {
            String className = path.getName();

            if (!path.getPath().equals(className)) {
                className = path.getPath();

                if (className.endsWith(".class")) {
                    className = className.substring(0, className.length() - 6);
                } else {
                    return;
                }

                className = className.replace(System.getProperty("file.separator"), ".");

                int packageNameIndex = className.lastIndexOf(packageName);

                if (packageNameIndex < 0) {
                    return;
                }
                className = className.substring(packageNameIndex);
            }

            try {
                Class<?> discoveredClass = Class.forName(className, false, classLoader);

                if (PreferenceReflectionUtils.isModel(discoveredClass)){
                    Class<? extends PreferenceModel> modelClass = (Class<? extends PreferenceModel>) discoveredClass;
                    mPreferences.put(modelClass, new PreferenceInfo(modelClass));
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }


}
