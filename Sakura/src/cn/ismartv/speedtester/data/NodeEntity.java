package cn.ismartv.speedtester.data;

/**
 * Created by huaijie on 14-10-30.
 */
public class NodeEntity {
    private String cdnID;
    private String flag;
    private String name;
    private String route_trace;
    private String url;
    private String ping;

    private String nick;

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    private int speed;

    public String getTestFile() {
        return "http://" + getUrl() + "/cdn/speedtest.ts";
    }

    public String getCdnID() {
        return cdnID;
    }

    public void setCdnID(String cdnID) {
        this.cdnID = cdnID;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getName() {
        return name.replace("|", "-").split("-")[0];
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoute_trace() {
        return route_trace;
    }

    public void setRoute_trace(String route_trace) {
        this.route_trace = route_trace;
    }

    public String getUrl() {
        return url.replace("|", "-").split("-")[0];
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNick() {
        return name.replace("|", "-").split("-")[1];
    }


}
