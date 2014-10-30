package cn.ismartv.speedtester.data;

import java.util.ArrayList;

/**
 * Created by huaijie on 14-10-30.
 */
public class HttpDataEntity {
    private static final String NO_RECORD = "104";
    private ArrayList<NodeEntity> cdn_list;

    private String retcode;
    private String retmsg;
    private SNCDNEntity sncdn;

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public SNCDNEntity getSncdn() {
        return sncdn;
    }

    public void setSncdn(SNCDNEntity sncdn) {
        this.sncdn = sncdn;
    }

    public ArrayList<NodeEntity> getCdn_list() {
        return cdn_list;
    }

    public void setCdn_list(ArrayList<NodeEntity> cdn_list) {
        this.cdn_list = cdn_list;
    }
}
