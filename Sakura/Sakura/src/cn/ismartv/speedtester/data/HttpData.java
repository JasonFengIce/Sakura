package cn.ismartv.speedtester.data;

import java.util.ArrayList;

/**
 * Created by fenghb on 14-7-11.
 */
public class HttpData {
    private ArrayList<Node> cdn_list;

    private String retcode;
    private String retmsg;
    private SNCDN sncdn;

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

    public SNCDN getSncdn() {
        return sncdn;
    }

    public void setSncdn(SNCDN sncdn) {
        this.sncdn = sncdn;
    }

    public ArrayList<Node> getCdn_list() {
        return cdn_list;
    }

    public void setCdn_list(ArrayList<Node> cdn_list) {
        this.cdn_list = cdn_list;
    }
}
