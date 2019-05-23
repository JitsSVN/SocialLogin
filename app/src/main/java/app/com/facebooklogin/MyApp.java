package app.com.facebooklogin;

import android.app.Application;

import app.com.tasociallogin.network.TASocialLogin;

public class MyApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
//        TAFacebookLogin.inItFirebaseAuth(this);
        TASocialLogin.init(this);
    }
}
