package legdk.ru.firebasechat.Register;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import legdk.ru.firebasechat.Adapter.UsersChatAdapter;
import legdk.ru.firebasechat.Helpers.ChatHelper;
import legdk.ru.firebasechat.Models.User;
import legdk.ru.firebasechat.R;
import legdk.ru.firebasechat.UI.MainActivity;

public class RegisterActivity extends Activity{

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @BindView(R.id.edit_text_display_name) EditText userFirstNameRegister;
    @BindView(R.id.edit_text_email_register) EditText userEmailRegister;
    @BindView(R.id.edit_text_password_register) EditText userPassWordRegister;

    private FirebaseAuth auth;
    private DatabaseReference database;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        hideActionBar();
        bindButterKnife();
        setAuthInstance();
        setDatabaseInstance();
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

    private void setDatabaseInstance() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    @OnClick(R.id.btn_register_user)
    public void registerUserClickListener(Button button) {
        onRegisterUser();
    }

    @OnClick(R.id.btn_cancel_register)
    public void cancelClickListener(Button button) {
        finish();
    }

    private void onRegisterUser() {
        if(getUserDisplayName().equals("") || getUserEmail().equals("") || getUserPassword().equals("")){
            showFieldsAreRequired();
        }else if(isIncorrectEmail(getUserEmail()) || isIncorrectPassword(getUserPassword())) {
            showIncorrectEmailPassword();
        }else {
            signUp(getUserEmail(), getUserPassword());
        }
    }

    private boolean isIncorrectEmail(String userEmail) {
        return !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches();
    }

    private boolean isIncorrectPassword(String userPassword) {
        return !(userPassword.length() >= 6);
    }

    private void showIncorrectEmailPassword() {
        showAlertDialog(getString(R.string.error_incorrect_email_pass), true);
    }

    private void showFieldsAreRequired() {
        showAlertDialog(getString(R.string.error_fields_empty), true);
    }

    private void showAlertDialog(String message, boolean isCancelable){

        dialog = ChatHelper.buildAlertDialog(getString(R.string.login_error_title),message,isCancelable,RegisterActivity.this);
        dialog.show();
    }

    private String getUserDisplayName() {
        return userFirstNameRegister.getText().toString().trim();
    }

    private String getUserEmail() {
        return userEmailRegister.getText().toString().trim();
    }

    private String getUserPassword() {
        return userPassWordRegister.getText().toString().trim();
    }


    private void signUp(String email, String password) {

        showAlertDialog("Registering...",true);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                dismissAlertDialog();

                if(task.isSuccessful()){
                    onAuthSuccess(task.getResult().getUser());
                }else {
                    showAlertDialog(task.getException().getMessage(), true);
                }
            }
        });
    }

    private void dismissAlertDialog() {
        dialog.dismiss();
    }

    private void onAuthSuccess(FirebaseUser user) {
        createNewUser(user.getUid());
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void createNewUser(String userId){
        User user = buildNewUser();
        database.child("users").child(userId).setValue(user);
    }

    private User buildNewUser() {
        return new User(
                getUserDisplayName(),
                getUserEmail(),
                UsersChatAdapter.ONLINE,
                ChatHelper.generateRandomAvatarForUser(),
                new Date().getTime()
                );
    }

}
