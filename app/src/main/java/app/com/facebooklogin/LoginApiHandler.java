package app.com.facebooklogin;

import android.view.View;

public class LoginApiHandler {
    LoginApiListener mLoginApiListener;

    public LoginApiHandler(LoginApiListener loginApiListener) {
        this.mLoginApiListener = loginApiListener;
    }

    public void onTwitterLoginClick(View v) {
        mLoginApiListener.onTwitterLoginClick();
    }

    public void onGoogleLoginClick(View view){
        mLoginApiListener.onGoogleLoginClick();
    }

    public void onFbLoginClick(View view){
        mLoginApiListener.onFbLoginClick();
    }

    public void onLogout(View view){
        mLoginApiListener.onLogout();
    }
}
