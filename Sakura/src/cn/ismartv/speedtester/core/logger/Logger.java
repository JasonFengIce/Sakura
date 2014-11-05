package cn.ismartv.speedtester.core.logger;

/**
 * Created by huaijie on 14-11-5.
 */


public class Logger {

    public static final String D = "d";
    public static final String E = "e";
    public static final String I = "i";

    public static final String DIVIDER = "-------------------------";


    private String level, tag, message;

    public Logger(String level, String tag, String message) {
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    public void log() {
        LoggerEntity loggerEntity = new LoggerEntity();
        loggerEntity.level = level;
        loggerEntity.tag = tag;
        loggerEntity.message = message;
        loggerEntity.save();
    }


    public static class Builder {


        private String level;
        private String tag;
        private StringBuffer message = new StringBuffer();

        public Builder setLevel(String level) {
            this.level = level;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setMessage(String message) {
            this.message.append(message + "\n");
            return this;
        }

        public Logger build() {

            return new Logger(level, tag, message.toString());
        }

    }


}