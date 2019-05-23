package app.com.facebooklogin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

import app.com.facebooklogin.databinding.ActivityMainBinding;
import app.com.tasociallogin.LoginTypeEnum.TALoginType;
import app.com.tasociallogin.network.TAGoogleLogin;
import app.com.tasociallogin.network.TASocialLogin;
import app.com.tasociallogin.network.AccessToken;
import app.com.tasociallogin.listener.OnLoginCompleteListener;
import app.com.tasociallogin.network.TAFacebookLogin;
import app.com.tasociallogin.network.TATwitterLogin;

public class MainActivity extends AppCompatActivity implements OnLoginCompleteListener, LoginApiListener {

    private List<String> fbScope = Arrays.asList("public_profile", "email");
    ActivityMainBinding activityMainBinding;
    private final String CustomerKey="NhwaLa0kbM54WavS6Cp4PLHb2";
    private final String ConsumerSecret="TbKFyPzi1GrihCsuvh9QF0YzNyAnW3HQvAPSKimJqcjJDrdeDY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        activityMainBinding.setHandler(new LoginApiHandler(this));

    }


    @Override
    public void onLoginSuccess(AccessToken token) {
        if (token != null) {
            activityMainBinding.tvText.setText(String.format("Name:-%s\nEmail:-%s\nUserId:-%s\nDp:-%s", token.getUserName(), token.getEmail(), token.getUserId(), token.getProfilePicture()));
            setImage(token.getProfilePicture());
            Log.d("MAIN", "FACEBOOK Login successful: " + token.getUserName() + "|||" + token.getEmail());
        }
    }

    private void setImage(String img) {
        Glide.with(this).load(img).into(activityMainBinding.ivDp);
    }

    @Override
    public void onLoginError(String errorMessage) {
        Log.e("MAIN", "ERROR!" + errorMessage);
        activityMainBinding.tvText.setText(errorMessage);
        setImage(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (TASocialLogin.getLoginType()== TALoginType.TAGOOGLE) {
            TAGoogleLogin.activityResult(this, requestCode, resultCode, data);
        }else if (TASocialLogin.getLoginType()== TALoginType.TAFACEBOOK) {
            TAFacebookLogin.onActivityResult(requestCode, resultCode, data);
        }else if (TASocialLogin.getLoginType()==TALoginType.TATWITTER){
            TATwitterLogin.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onTwitterLoginClick() {
        TATwitterLogin.requestLogin(this,CustomerKey,ConsumerSecret,this);
    }

    @Override
    public void onGoogleLoginClick() {
        activityMainBinding.tvText.setText("");
        setImage(null);
        TAGoogleLogin.googleLogin(this, this, getString(R.string.default_web_client_id));

    }

    @Override
    public void onFbLoginClick() {
        activityMainBinding.tvText.setText("");
        setImage(null);
        TAFacebookLogin.requestLogin(this, fbScope, this);
    }

    @Override
    public void onLogout() {
        if (TASocialLogin.checkUserSignIn()) {
            activityMainBinding.tvText.setText("Logout Successfully");
            setImage(null);
            TASocialLogin.signOut();
        }
    }
}
