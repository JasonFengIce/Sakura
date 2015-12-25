package cn.ismartv.iris.data.table;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by huaijie on 6/24/15.
 */

@Table(name = "download", id = "_id")
public class DownloadTable extends Model {
    public static final String FILE_NAME = "file_name";
    public static final String URL = "url";
    public static final String DOWNLOAD_PATH = "download_path";
    public static final String MD5 = "md5";
    public static final String DOWNLOAD_STATE = "download_state";

    @Column
    public String file_name;

    @Column
    public String url;

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String download_path;

    @Column
    public String local_md5;

    @Column
    public String server_md5;

    @Column
    public String download_state;
}
