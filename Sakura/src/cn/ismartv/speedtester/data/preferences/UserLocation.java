package cn.ismartv.speedtester.data.preferences;

import cn.ismartv.preferences.PreferenceModel;
import cn.ismartv.preferences.annotation.Preference;
import cn.ismartv.preferences.annotation.PreferenceItem;

/**
 * Created by huaijie on 1/22/15.
 */

@Preference(name = "user_location_info")
public class UserLocation extends PreferenceModel {

    @PreferenceItem(name = "user_default_city")
    public String userDefaultCity = "";


    @PreferenceItem(name = "user_default_isp")
    public String userDefaultIsp = "";

}
