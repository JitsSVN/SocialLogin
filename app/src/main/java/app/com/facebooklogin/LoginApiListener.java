package app.com.facebooklogin;

public interface LoginApiListener {
     void onTwitterLoginClick();
     void onGoogleLoginClick();
     void onFbLoginClick();
     void onLogout();
}
