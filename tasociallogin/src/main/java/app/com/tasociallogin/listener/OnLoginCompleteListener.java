package app.com.tasociallogin.listener;

import app.com.tasociallogin.network.AccessToken;

public interface OnLoginCompleteListener {

    /**
     * Called when login complete.
     */
    void onLoginSuccess(AccessToken accessToken);

    /**
     * called when get error
     * @param errorMessage
     */
    void onLoginError(String errorMessage);
}
