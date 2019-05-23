package app.com.tasociallogin.network;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import app.com.tasociallogin.LoginTypeEnum.TALoginType;
import app.com.tasociallogin.R;

public class TASocialLogin {
    private static FirebaseAuth mAuth;
    private static TALoginType loginType;

    /**
     * initalise FireBaseApp and getInstance of Firebase Auth
     *
     * @param context
     */
    public static void init(Context context) {
        if (FirebaseAuth.getInstance() != null) {
            FirebaseApp.initializeApp(context);
            mAuth = FirebaseAuth.getInstance();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.initislise_firebase), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get FireBase Auth from library
     *
     * @return
     */
    static FirebaseAuth getAuth() {
        return mAuth;
    }

    /**
     * user signOut
     */
    public static void signOut() {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * to get LoginType
     * @return
     */
    public static TALoginType getLoginType() {
        return loginType;
    }

    /**
     * set Login Type from TAFacebook,TAGoogle and TATwitter
     * @param loginType
     */
    static void setLoginType(TALoginType loginType) {
        TASocialLogin.loginType = loginType;
    }

    /**
     * check user signIn status
     */
    public static boolean checkUserSignIn() {
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return false;
            else return true;
        }
        return false;
    }

    /**
     * retrieve Email from FirebaseUser
     *
     * @param firebaseUser
     * @return
     */
    protected static String getEmail(FirebaseUser firebaseUser) {
        String email = "";
        for (int i = 0; i < firebaseUser.getProviderData().size(); i++) {
            if (!TextUtils.isEmpty(firebaseUser.getProviderData().get(i).getEmail())) {
                email = firebaseUser.getProviderData().get(i).getEmail();
                break;
            }
        }
        return email;
    }
}
