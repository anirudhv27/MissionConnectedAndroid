package com.avaliveru.missionconnected;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
    private AutoCompleteTextView schoolName;
    private Button registerSchoolButton;
    private FirebaseUser currentUser;
    private ArrayList<String> domains ;
    private ArrayList<String> special_users;
    //private String schoolname;
    private String schoolID;
    private ArrayList<String> schoolIDs;
    private ArrayList<String> schoolNames;
    private AutoCompleteTextView userSchool;
    ArrayAdapter<String> clubAdapter;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_school);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        domains = new ArrayList<>();
        special_users = new ArrayList<>();

        registerSchoolButton = findViewById(R.id.registerSchoolButton);
        registerSchoolButton.setOnClickListener(this);


        fetchSchoolDetails();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(FirebaseAuth.getInstance() != null)
            FirebaseAuth.getInstance().signOut();
    }

    private void fetchSchoolDetails() {
        schoolNames = new ArrayList<>();
        schoolIDs = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("schools").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                        schoolIDs.add(child.getKey());
                        schoolNames.add(child.child("school_name").getValue().toString());
                    }
                userSchool = findViewById(R.id.schoolNameInput);
                clubAdapter= new ArrayAdapter<>(PickSchoolActivity.this, android.R.layout.simple_list_item_1, schoolNames);
                userSchool.setAdapter(clubAdapter);

                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        AutoCompleteTextView schoolNameView = findViewById(R.id.schoolNameInput);
        String school = schoolNameView.getText().toString().trim();

        if(TextUtils.isEmpty(school) || ! schoolNames.contains(school)){
            alert(getString(R.string.empty_school_register_error));
            return;
        }
        String schoolID = schoolIDs.get(schoolNames.indexOf(school));
        setSchoolData(schoolID, currentUser.getEmail());
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
        alertDialog2.setTitle("Logout");
        alertDialog2.setMessage("Are you sure you want to log out?");
        alertDialog2.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        GoogleSignInClient mGoogleSignInClient ;
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();
                        mGoogleSignInClient = GoogleSignIn.getClient(PickSchoolActivity.this, gso);
                        mGoogleSignInClient.signOut().addOnCompleteListener(PickSchoolActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseAuth.getInstance().signOut();
                                Intent i = new Intent(PickSchoolActivity.this, GoogleSignInActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        });
                    }
                });
        alertDialog2.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        PickSchoolActivity.this.onBackPressed();
                    }
                });
        alertDialog2.show();
    }
}