package com.avaliveru.missionconnected;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PickSchoolActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PickSchool";
    AutoCompleteTextView schoolName;
    Button registerSchoolButton;
    FirebaseUser currentUser;
    ArrayList<String> domains ;
    ArrayList<String> special_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_school);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        domains = new ArrayList<>();
        special_users = new ArrayList<>();

        registerSchoolButton = findViewById(R.id.registerSchoolButton);
        registerSchoolButton.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        AutoCompleteTextView schoolNameView = findViewById(R.id.schoolNameInput);
        String school = schoolNameView.getText().toString().trim();
        school = "missionsanjosehigh";
        if(TextUtils.isEmpty(school)){
            alert(getString(R.string.empty_school_register_error));
            return;
        }
        setSchoolData(school, currentUser.getEmail());
    }


    private void setSchoolData(final String school, final String email) {
        FirebaseDatabase.getInstance().getReference()
                .child("schools").child(school).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot domainSnap = snapshot.child("domains");
                DataSnapshot specialUsers = snapshot.child("special_users");
                for (DataSnapshot child : domainSnap.getChildren())
                    domains.add((String) child.getValue());
                for (DataSnapshot child : specialUsers.getChildren())
                    special_users.add((String) child.getValue());

                // see if email is valid
                String emailDomain = email.split("@")[1];
                if(special_users.contains(email) || domains.contains(emailDomain)  ){
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference usersRef = dbRef.child("users");

                    String key = currentUser.getUid();
                    usersRef.child(key).child("email").setValue(currentUser.getEmail());
                    usersRef.child(key).child("fullname").setValue(currentUser.getDisplayName());
                    usersRef.child(key).child("isAdmin").setValue(false);
                    usersRef.child(key).child("school").setValue(school);
                    if (currentUser.getPhotoUrl()!=null) {
                        usersRef.child(key).child("imgurl").setValue(currentUser.getPhotoUrl().toString());
                    }
                    Intent newIntent = new Intent(PickSchoolActivity.this, MainActivity.class);
                    startActivity(newIntent);
                }else {
                    alertLoginError(getString(R.string.invalid_loginid_alert));
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alertLoginError(String message) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder( this);
        alertDialog2.setTitle("Alert");
        alertDialog2.setMessage(message);
        alertDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();
                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
                        mGoogleSignInClient.signOut();
                        FirebaseAuth.getInstance().signOut();
                        dialog.cancel();
                        finish();

                    }
                });
        alertDialog2.show();
    }
    private void alert(String s) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder( this);
        alertDialog2.setTitle("Alert");
        alertDialog2.setMessage(s);
        alertDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog2.show();
    }

}