package app.com.tasociallogin.network;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import app.com.tasociallogin.LoginTypeEnum.TALoginType;
import app.com.tasociallogin.listener.OnLoginCompleteListener;

import static android.app.Activity.RESULT_OK;

/**
 * Created by deepaksharma on 4/6/18.
 */

public class TAGoogleLogin {
    public static final int RC_SIGN_IN = 234;
    private static @NonNull FirebaseAuth mAuth = TASocialLogin.getAuth();
    private static String TAG = TAGoogleLogin.class.getSimpleName();
    private static OnLoginCompleteListener mGoogleResponse;



    /**
     * @param loginCompleteListener is describe user login status
     * @param  clientId is default_web_client_id
     */
    public static void googleLogin(Activity activity, OnLoginCompleteListener loginCompleteListener, String clientId) {
        mGoogleResponse = loginCompleteListener;
        TASocialLogin.setLoginType(TALoginType.TAGOOGLE);
        if (mAuth.getCurrentUser() == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(clientId).requestEmail().build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            getUserInfo(mAuth.getCurrentUser());
        }
    }

    public static void activityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(activity, account);
            } catch (ApiException e) {
                Log.d(TAG, "ApiException: " + e.getMessage());
                mGoogleResponse.onLoginError("Authentication failed.");
            }
        } else {
            mGoogleResponse.onLoginError("Authentication failed.");
            Log.d(TAG, "RequestCode -> " + resultCode);
        }
    }

    private static void firebaseAuthWithGoogle(final Activity activity, final GoogleSignInAccount account) {
        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser=mAuth.getCurrentUser();
                    assert firebaseUser != null;
                    getUserInfo(firebaseUser);
                } else {
                    mGoogleResponse.onLoginError("Authentication failed.");
                }
            }
        });
    }
    /**
     * @param  firebaseUser is current login user
     */
    private static void getUserInfo(@NonNull FirebaseUser firebaseUser) {
        app.com.tasociallogin.network.AccessToken accessToken = new app.com.tasociallogin.network.AccessToken.Builder(firebaseUser.getProviderId())
                .userId(firebaseUser.getUid())
                .email(TASocialLogin.getEmail(firebaseUser))
                .email(firebaseUser.getEmail())
                .userName(firebaseUser.getDisplayName())
                .profilePictur(String.valueOf(firebaseUser.getPhotoUrl()))
                .build();
        mGoogleResponse.onLoginSuccess(accessToken);
    }

}
