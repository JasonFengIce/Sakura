package cn.ismartv.speedtester.data.http;

import java.util.HashMap;

/**
 * Created by huaijie on 1/23/15.
 */
public class EventInfoEntity {

    private String event;

    private HashMap<String, String> properties;


    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
}
