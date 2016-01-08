package tv.ismar.sakura;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.sakura.core.initialization.InitializeProcess;


public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this, true);
        new Thread(new InitializeProcess(this)).start();

    }


}
