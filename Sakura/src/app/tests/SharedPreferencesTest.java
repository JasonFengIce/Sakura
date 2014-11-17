package app.tests;

import cn.ismartv.speedtester.preferences.Model;
import cn.ismartv.speedtester.preferences.annotation.Item;
import cn.ismartv.speedtester.preferences.annotation.Preference;

/**
 * Created by huaijie on 11/17/14.
 */

@Preference(name = "test")
public class SharedPreferencesTest extends Model {

    @Item(name = "test1")
    public String test1 = "";

    @Item(name = "test2")
    public String test2 = "";
}
