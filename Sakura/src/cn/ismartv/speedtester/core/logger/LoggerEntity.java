package cn.ismartv.speedtester.core.logger;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huaijie on 14-11-5.
 */

@Table(name = "logger")
public class LoggerEntity extends Model {
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String LEVEL = "level";
    public static final String TAG = "tag";
    public static final String MESSAGE = "message";

    @Column(name = TIME)
    public long time = System.currentTimeMillis();

    @Column(name = DATE)
    public String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

    @Column(name = LEVEL)
    public String level = "i";

    @Column(name = TAG)
    public String tag = "";

    @Column(name = MESSAGE)
    public String message = "";

}

