package cn.ismartv.speedtester.data;

/**
 * Created by huaijie on 8/7/14.
 */
public class VersionInfo {
    private String version;
    private String md5;
    private String downloadurl;
    private String speedlogurl;

    public String getSpeedlogurl() {
        return speedlogurl;
    }

    public void setSpeedlogurl(String speedlogurl) {
        this.speedlogurl = speedlogurl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }


}
