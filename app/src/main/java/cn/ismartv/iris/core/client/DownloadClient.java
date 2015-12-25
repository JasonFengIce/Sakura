package cn.ismartv.iris.core.client;

import android.content.Context;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.*;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "DownloadClient";

    private String url;
    private File downloadFile;
    private String mServerMD5;
    private String mSaveName;

    private StoreType mStoreType;
    private Context mContext;

    private FileOutputStream fileOutputStream;


    public DownloadClient(Context context, String downloadUrl, String saveName, StoreType storeType) {
        mContext = context;
        url = downloadUrl;
//        mServerMD5 = FileUtils.getFileByUrl(downloadUrl).split("\\.")[0];
        mStoreType = storeType;
        mSaveName = saveName;

        switch (mStoreType) {
            case Internal:
                try {
                    fileOutputStream = mContext.openFileOutput(mSaveName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                    downloadFile = mContext.getFileStreamPath(mSaveName);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFoundException");
                    return;
                }
                break;
            case External:
//                try {
//                    downloadFile = new File(HardwareUtils.getSDCardCachePath(), mSaveName);
//                    if (!downloadFile.exists()) {
//                        downloadFile.getParentFile().mkdirs();
//                        downloadFile.createNewFile();
//                    }
//                    fileOutputStream = new FileOutputStream(downloadFile, false);
//                } catch (FileNotFoundException e) {
//                    Log.e(TAG, "FileNotFoundException");
//                    return;
//                } catch (IOException e) {
//                    Log.e(TAG, "IOException");
//                }
                break;
        }
    }


    @Override
    public void run() {
        //database
//        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.DOWNLOAD_PATH + " =? ", downloadFile.getAbsolutePath()).executeSingle();
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, "IOException");
        }

//        downloadTable.download_path = downloadFile.getAbsolutePath();
//        downloadTable.download_state = DownloadState.complete.name();
//        downloadTable.local_md5 = HardwareUtils.getMd5ByFile(downloadFile);
//        downloadTable.save();
//
//
//        Log.d(TAG, "url is: " + url);
//        Log.d(TAG, "server md5 is: " + mServerMD5);
//        Log.d(TAG, "local md5 is: " + downloadTable.local_md5);
//        Log.d(TAG, "download complete!!!");

    }

    public enum StoreType {
        Internal,
        External
    }

    public enum DownloadState {
        run,
        pause,
        complete
    }

    public String getUrl() {
        return url;
    }

    public File getDownloadFile() {
        return downloadFile;
    }

    public String getmServerMD5() {
        return mServerMD5;
    }

    public StoreType getmStoreType() {
        return mStoreType;
    }

    public String getmSaveName() {
        return mSaveName;
    }


}

//                    Request request = new Request.Builder()
//                            .url(url)
//                            .addHeader("RANGE", "bytes=" + localFile.length() + "-")
//                            .build();
//                    response = client.newCall(request).execute();
//                    fileOutputStream = new FileOutputStream(localFile, true);
//
