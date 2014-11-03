package cn.ismartv.speedtester.provider;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 14-10-30.
 */

@Table(name = "nodes")
public class NodeCacheTable extends Model {
    public static final String CDN_ID = "cdn_id";
    public static final String URL = "url";
    public static final String ID = "_id";
    public static final String SPEED = "speed";

    @Column(name = "cdn_id", uniqueGroups = {"group1"}, onUniqueConflicts = {Column.ConflictAction.IGNORE})
    public int cdnID = 0;

    @Column(name = "node_name")
    public String nodeName = "";

    @Column(name = "nick")
    public String nick = "";

    @Column(name = "flag")
    public String flag = "";

    @Column(name = "area")
    public int area = 0;

    @Column(name = "isp")
    public int isp = 0;

    @Column(name = "ip")
    public String ip = "";

    @Column(name = "url")
    public String url = "";

    @Column(name = "route_trace")
    public String routeTrace = "";

    @Column(name = "speed")
    public int speed = 0;

    @Column(name = "update_time")
    public String updateTime = "";

    @Column(name = "checked")
    public String checked = "";

    @Column(name = "running")
    public String running = "";
}

