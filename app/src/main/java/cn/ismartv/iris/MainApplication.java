package cn.ismartv.iris;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import cn.ismartv.iris.core.client.IsmartvUrlClient;
import cn.ismartv.iris.core.initialization.InitializeProcess;


public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this, true);
        new Thread(new InitializeProcess(this)).start();
        IsmartvUrlClient.initializeWithContext(this);

    }


}
