package cn.ismartv.speedtester.data;

import java.util.List;
import java.util.Map;

/**
 * Created by huaijie on 8/4/14.
 */
public class FeedBack {

    private String city;
    private String descriptionl;
    private String ip;
    private String phone;
    private String isp;
    private String location;
    private String mail;
    private String option;
    private String is_correct;
    private List<Map<String, String>> speed;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescriptionl() {
        return descriptionl;
    }

    public void setDescriptionl(String descriptionl) {
        this.descriptionl = descriptionl;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(String is_correct) {
        this.is_correct = is_correct;
    }

    public List<Map<String, String>> getSpeed() {
        return speed;
    }

    public void setSpeed(List<Map<String, String>> speed) {
        this.speed = speed;
    }
}
