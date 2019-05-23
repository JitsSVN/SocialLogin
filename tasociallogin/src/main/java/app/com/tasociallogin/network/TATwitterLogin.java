package app.com.tasociallogin.network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import app.com.tasociallogin.LoginTypeEnum.TALoginType;
import app.com.tasociallogin.listener.OnLoginCompleteListener;


public class TATwitterLogin {

    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;

    private static TwitterAuthClient client;

    private static OnLoginCompleteListener listener;

    private static FirebaseAuth mAuth = TASocialLogin.getAuth();

    private static Callback<TwitterSession> buttonCallback = new Callback<TwitterSession>() {

        @Override
        public void success(Result<TwitterSession> result) {
            TwitterSession session = result.data;
            handleTwitterSession(session);
        }

        @Override
        public void failure(TwitterException e) {
            listener.onLoginError(e.getMessage());
        }
    };

    public static void requestLogin(Activity activity, String consumerKey,
                             String consumerSecret, OnLoginCompleteListener onLoginCompleteListener) {
        mActivity = activity;
        listener = onLoginCompleteListener;
        TASocialLogin.setLoginType(TALoginType.TATWITTER);
        if (mAuth.getCurrentUser() == null) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(consumerKey, consumerSecret);
            TwitterConfig config = new TwitterConfig.Builder(activity.getApplicationContext()).logger(new DefaultLogger(Log.DEBUG)).twitterAuthConfig(authConfig).build();
            Twitter.initialize(config);
            client = new TwitterAuthClient();
            client.authorize(activity, buttonCallback);
        } else {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            loggedInUser(firebaseUser);
        }
    }

    private static void loggedInUser(FirebaseUser user) {
        AccessToken accessToken = new AccessToken.Builder(user.getProviderId())
                .userName(user.getDisplayName())
                .userId(String.valueOf(user.getUid()))
                .profilePictur(String.valueOf(user.getPhotoUrl()))
                .email(user.getEmail())
                .build();
        listener.onLoginSuccess(accessToken);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (client != null)
            client.onActivityResult(requestCode, resultCode, data);
    }


    private static void handleTwitterSession(final TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token, session.getAuthToken().secret);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            fetchTwitterEmail(session,user);
                            loggedInUser(user);
                        } else {
                            listener.onLoginError("Authentication failed.");
                        }
                    }
                });
    }

    /**
     * Before using this feature, ensure that “Request email addresses from users” is checked for your Twitter app.
     * @param twitterSession user logged in twitter session
     */
    private static void fetchTwitterEmail(final TwitterSession twitterSession, final FirebaseUser user) {
        client.requestEmail(twitterSession, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                Log.e("Twitter", "success: "+result.data );
                //here it will give u only email and rest of other information u can get from TwitterSession
//                userDetailsLabel.setText("User Id : " + twitterSession.getUserId() + "\nScreen Name : " + twitterSession.getUserName() + "\nEmail Id : " + result.data);
            }

            @Override
            public void failure(TwitterException exception) {

            }
        });
    }
}
