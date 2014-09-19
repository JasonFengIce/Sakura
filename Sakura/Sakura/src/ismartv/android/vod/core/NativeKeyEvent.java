package ismartv.android.vod.core;

/**
 * Created by <huaijiefeng@gmail.com> on 8/20/14.
 */
public class NativeKeyEvent {
    static {
        System.loadLibrary("input");
    }

    public native int enableDebug(int enable);

    public native int create(String dev, int kb, int mouse);

    public native int close(int fd);

    public native int sendEvnet(int fd, int type, int code, int value);


}
