package cn.ismartv.speedtester.others;

import android.os.SystemClock;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;

/**
 * NetworkUtils is a utility Class for download file throughout http protocol.and update information about downloading progress.
 *
 * @author bob
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    public static int getFileFromUrl(NetworkSpeedInfo networkSpeedInfo) {
//		byte[] fileBytes = null;
//		int bytecount = 0;
        URL fileUrl = null;
        HttpURLConnection conn = null;
        InputStream stream = null;
        try {
            fileUrl = new URL(networkSpeedInfo.url);
            conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            networkSpeedInfo.realUrl = conn.getURL().toString();
            networkSpeedInfo.filesize = conn.getContentLength();
            stream = conn.getInputStream();
//			fileBytes = new byte[fileLength];
            networkSpeedInfo.timeStarted = SystemClock.uptimeMillis();
            int actuallyReadCount = 0;
            byte[] buff = new byte[128];
            while ((actuallyReadCount = stream.read(buff)) != -1 && networkSpeedInfo.flagStop == 0) {
                networkSpeedInfo.filesizeFinished += actuallyReadCount;
            }
        } catch (SocketTimeoutException e) {
//			e.printStackTrace();
            return MainActivity.NETWORK_CONNECTION_TIMEOUT;
        } catch (Exception e) {
//			e.printStackTrace();
            return MainActivity.NETWORK_CONNECTION_UNKNOWN;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return MainActivity.NETWORK_CONNECTION_NORMAL;
    }

    public static String uploadString(String str, String uploadURL) {
        String localeName = Locale.getDefault().toString();
        URL url = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        OutputStream outputStream = null;
        BufferedWriter writer = null;
        try {
//			url = new URL(uploadURL+"?q="+URLEncoder.encode(str, "UTF-8"));
            url = new URL(uploadURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("content-type", "text/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", android.os.Build.MODEL.replaceAll(" ", "_") + "/" + android.os.Build.ID + " " + android.os.Build.SERIAL);
            conn.setRequestProperty("Accept-Language", localeName);
            outputStream = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write("q=" + str);
            writer.flush();
//			conn.connect();
            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                inputStream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }


            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
        return sb.toString();

    }

    public static String charEncoder(String str) {
        StringBuffer sb = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) {
            sb.append("\\u").append(Integer.toHexString((int) str.charAt(i) & 0xffff));
        }

        return sb.toString();
    }
}
