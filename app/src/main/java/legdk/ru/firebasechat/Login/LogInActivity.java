package legdk.ru.firebasechat.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import legdk.ru.firebasechat.Adapter.UsersChatAdapter;
import legdk.ru.firebasechat.Helpers.ChatHelper;
import legdk.ru.firebasechat.R;
import legdk.ru.firebasechat.Register.RegisterActivity;
import legdk.ru.firebasechat.UI.MainActivity;

public class LogInActivity extends Activity {

    private static final String TAG = LogInActivity.class.getSimpleName();
    @BindView(R.id.edit_text_email_login) EditText userEmail;
    @BindView(R.id.edit_text_password_log_in) EditText userPassWord;

    private FirebaseAuth auth;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        hideActionBar();
        bindButterKnife();
        setAuthInstance();
    }

    private void hideActionBar() {
        this.getActionBar().hide();
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setAuthInstance() {
        auth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_login)
    public void logInClickListener(Button button) {
        onLogInUser();
    }

    @OnClick(R.id.btn_register)
    public void registerClickListener(Button button) {
        goToRegisterActivity();
    }

    private void onLogInUser() {
        if(getUserEmail().equals("") || getUserPassword().equals("")){
            showFieldsAreRequired();
        }else {
            logIn(getUserEmail(), getUserPassword());
        }
    }

    private void showFieldsAreRequired() {
        showAlertDialog(getString(R.string.error_incorrect_email_pass),true);
    }

    private void logIn(String email, String password) {

        showAlertDialog("Log In...",false);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                dismissAlertDialog();

                if(task.isSuccessful()){
                    setUserOnline();
                    goToMainActivity();
                }else {
                    showAlertDialog(task.getException().getMessage(),true);
                }
            }
        });
    }

    private void setUserOnline() {
        if(auth.getCurrentUser()!=null ) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance()
                    .getReference().
                    child("users").
                    child(userId).
                    child("connection").
                    setValue(UsersChatAdapter.ONLINE);
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private String getUserEmail() {
        return userEmail.getText().toString().trim();
    }

    private String getUserPassword() {
        return userPassWord.getText().toString().trim();
    }

    private void showAlertDialog(String message, boolean isCancelable){
        dialog = ChatHelper.buildAlertDialog(getString(R.string.login_error_title), message,isCancelable,LogInActivity.this);
        dialog.show();
    }

    private void dismissAlertDialog() {
        dialog.dismiss();
    }
}
