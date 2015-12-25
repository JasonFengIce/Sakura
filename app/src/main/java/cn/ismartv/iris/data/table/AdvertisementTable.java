package cn.ismartv.iris.data.table;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 7/31/15.
 */
@Table(name = "advertisement", id = "_id")
public class AdvertisementTable extends Model {
    public static final String TITLE = "title";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String EVERYDAY_TIME_FROM = "everyday_time_from";
    public static final String EVERYDAY_TIME_TO = "everyday_time_to";
    public static final String URL = "url";
    public static final String LOCATION = "location";
    public static final String MD5 = "md5";
    public static final String TYPE = "type";

    @Column
    public String title;

    @Column
    public long start_date;

    @Column
    public long end_date;

    @Column
    public long everyday_time_from;

    @Column
    public long everyday_time_to;

    @Column
    public String url;

    @Column
    public String location;

    @Column
    public String md5;

    @Column
    public String type;

    @Column
    public String media_id;

}
