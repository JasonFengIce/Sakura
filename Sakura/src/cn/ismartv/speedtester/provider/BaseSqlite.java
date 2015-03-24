package cn.ismartv.speedtester.provider;

import java.util.ArrayList;

/**
 * Created by huaijie on 2015/3/24.
 */
public interface BaseSqlite {
    public static final String SPACE = " ";

    public static final String CREATE = "CREATE";
    public static final String DROP = "DROP";


    public static final String TABLE = "TABLE";

    public static final String INTEGER = "INTEGER";
    public static final String TEXT = "TEXT ";

    public static final String PRIMARY_KEY = " PRIMARY KEY ";

    public static final String AUTO_INCREMENT = " AUTOINCREMENT ";

    class TableBuilder {
        private String name;
        private ArrayList<StringBuffer> rows = new ArrayList<>();

        public TableBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public TableBuilder setRow(String key, String type, String... extras) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(key.trim()).append(SPACE).append(type.trim())
                    .append(extras.length == 0 ? "" : extras[0].trim());
            rows.add(stringBuffer);
            return this;
        }

        public String create() {
            final int LAST = rows.size() - 1;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < rows.size(); i++) {
                if (i == 0) {
                    stringBuffer.append(CREATE).append(SPACE).append(TABLE).append(SPACE).append(name).append(SPACE);
                    stringBuffer.append("(").append(rows.get(i)).append(rows.size() == 1 ? "" : ", ");
                } else if (i == LAST) {
                    stringBuffer.append(rows.get(i));
                } else {
                    stringBuffer.append(rows.get(i)).append(", ");
                }
            }
            stringBuffer.append(");");
            return stringBuffer.toString();
        }
    }


    class DropBuilder {
        public String create(String name) {
            StringBuffer stringBuffer = new StringBuffer();
            return stringBuffer.append("DROP TABLE IF EXISTS ").append(name).toString();
        }
    }
}
