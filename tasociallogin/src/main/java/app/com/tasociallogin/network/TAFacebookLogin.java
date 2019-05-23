package app.com.tasociallogin.network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import app.com.tasociallogin.LoginTypeEnum.TALoginType;
import app.com.tasociallogin.R;
import app.com.tasociallogin.listener.OnLoginCompleteListener;


public class TAFacebookLogin {

    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;

    private static CallbackManager callbackManager;

    private static OnLoginCompleteListener listener;

    private static FirebaseAuth mAuth = TASocialLogin.getAuth();

    private static final String TAG = TAFacebookLogin.class.getSimpleName();

    private static FacebookCallback<LoginResult> loginCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            final AccessToken fbAccessToken = loginResult.getAccessToken();
            handleFacebookAccessToken(fbAccessToken);
        }

        @Override
        public void onCancel() {
            listener.onLoginError(mActivity.getResources().getString(R.string.sign_in_fail));
        }

        @Override
        public void onError(FacebookException error) {
            listener.onLoginError(error.getMessage());
        }
    };

    /**
     * request the login for facebook from activity or fragment
     * @param activity
     * @param permissions
     * @param loginCompleteListener
     */
    public static void requestLogin(Activity activity, List<String> permissions,
                                    OnLoginCompleteListener loginCompleteListener) {
        mActivity = activity;
        listener = loginCompleteListener;
        TASocialLogin.setLoginType(TALoginType.TAFACEBOOK);
        try {
            if (mAuth.getCurrentUser() == null) {
                setLoginCallBack(activity, permissions);
            } else {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                loggedInUserInfo(firebaseUser, AccessToken.getCurrentAccessToken());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set LoginManager Callback and read permissions
     * @param activity
     * @param permissions
     */
    private static void setLoginCallBack(Activity activity, List<String> permissions) {
        callbackManager = CallbackManager.Factory.create();
        String applicationID = Utility.getMetadataApplicationId(mActivity);

        if (applicationID == null) {
            throw new IllegalStateException(activity.getString(R.string.application_id_cant_be_null) +
                    activity.getString(R.string.please_check));
        }
        LoginManager.getInstance().logInWithReadPermissions(mActivity, permissions);
        LoginManager.getInstance().registerCallback(callbackManager, loginCallback);
    }

    /**
     * call from onActivityResult from activity or fragment to call callbackManager onActivityResult()
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * to complete signIn from firebase with valid credential and give output from firebase
     * @param token
     */
    private static void handleFacebookAccessToken(final AccessToken token) {

        try {
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = task.getResult().getUser();
                                loggedInUserInfo(firebaseUser, token);
                            } else {
                                listener.onLoginError(mActivity.getResources().getString(R.string.sign_in_fail));
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onLoginError(e.getMessage());
        }
    }

    private static void loggedInUserInfo(FirebaseUser firebaseUser, AccessToken token) {
        app.com.tasociallogin.network.AccessToken accessToken = new app.com.tasociallogin.network.AccessToken.Builder(token.getToken())
                .userId(firebaseUser.getUid())
                .email(TASocialLogin.getEmail(firebaseUser))
                .userName(firebaseUser.getDisplayName())
                .profilePictur(String.valueOf(firebaseUser.getPhotoUrl()))
                .build();
        listener.onLoginSuccess(accessToken);
    }


    /**
     * for printHashKey which is required at time to set up project
     * @param pContext
     * @return
     */
    public static String printHashKey(Context pContext) {
        String hashKey = null;
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                hashKey = new String(Base64.encode(md.digest(), 0));
                return hashKey;
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
        return hashKey;
    }
}
