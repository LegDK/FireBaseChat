package legdk.ru.firebasechat.UI;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import legdk.ru.firebasechat.Adapter.UsersChatAdapter;
import legdk.ru.firebasechat.Login.LogInActivity;
import legdk.ru.firebasechat.Models.User;
import legdk.ru.firebasechat.R;

public class MainActivity extends AppCompatActivity {

    private static String TAG =  MainActivity.class.getSimpleName();

    @BindView(R.id.progress_bar_users) ProgressBar progressBarForUsers;
    @BindView(R.id.recycler_view_users) RecyclerView usersRecyclerView;

    private String currentUserUid;
    private List<String> usersKeyList;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference userRefDatabase;
    private ChildEventListener childEventListener;
    private UsersChatAdapter usersChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        auth = FirebaseAuth.getInstance();
        userRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        setUserRecyclerView();
        usersKeyList = new ArrayList<String>();
        setAuthListener();
    }



    private void setUserRecyclerView() {
        usersChatAdapter = new UsersChatAdapter(this, new ArrayList<User>());
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setAdapter(usersChatAdapter);
    }


    private void setAuthListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                hideProgressBarForUsers();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    setUserData(user);
                    queryAllUsers();
                } else {
                    // User is signed out
                    goToLogin();
                }
            }
        };
    }

    private void setUserData(FirebaseUser user) {
        currentUserUid = user.getUid();
    }

    private void queryAllUsers() {
        childEventListener = getChildEventListener();
        userRefDatabase.limitToFirst(50).addChildEventListener(childEventListener);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // LoginActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressBarForUsers();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        clearCurrentUsers();

        if (childEventListener != null) {
            userRefDatabase.removeEventListener(childEventListener);
        }

        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }

    }

    private void clearCurrentUsers() {
        usersChatAdapter.clear();
        usersKeyList.clear();
    }

    private void logout() {
        showProgressBarForUsers();
        setUserOffline();
        auth.signOut();
    }

    private void setUserOffline() {
        if(auth.getCurrentUser()!=null ) {
            String userId = auth.getCurrentUser().getUid();
            userRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProgressBarForUsers(){
        progressBarForUsers.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarForUsers(){
        if(progressBarForUsers.getVisibility()==View.VISIBLE) {
            progressBarForUsers.setVisibility(View.GONE);
        }
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()){

                    String userUid = dataSnapshot.getKey();

                    if(dataSnapshot.getKey().equals(currentUserUid)){
                        User currentUser = dataSnapshot.getValue(User.class);
                        usersChatAdapter.setCurrentUserInfo(userUid, currentUser.getEmail(), currentUser.getCreatedAt());
                    }else {
                        User recipient = dataSnapshot.getValue(User.class);
                        recipient.setRecipientId(userUid);
                        usersKeyList.add(userUid);
                        usersChatAdapter.refill(recipient);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if(!userUid.equals(currentUserUid)) {

                        User user = dataSnapshot.getValue(User.class);

                        int index = usersKeyList.indexOf(userUid);
                        if(index > -1) {
                            usersChatAdapter.changeUser(index, user);
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
