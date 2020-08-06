package com.avaliveru.missionconnected;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
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
import java.util.Objects;


public class GoogleSignInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GoogleSignInActivity";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    FirebaseUser currentUser;
    private ArrayList<String> domains;
    private ArrayList<String> special_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);
        domains = new ArrayList<>();
        special_users = new ArrayList<>();

        //building data for email check
        FirebaseDatabase.getInstance().getReference()
                .child("schools").child("missionsanjosehigh").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot domainSnap = snapshot.child("domains");
                DataSnapshot specialUsers = snapshot.child("special_users");
                for (DataSnapshot child : domainSnap.getChildren())
                     domains.add((String) child.getValue());
                for (DataSnapshot child : specialUsers.getChildren())
                    special_users.add((String) child.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //end data to check email validity/////

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!= null){
            Intent newIntent = new Intent(GoogleSignInActivity.this, MainActivity.class);
            startActivity(newIntent);
        }else {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            signInButton = findViewById(R.id.signInButton);
            signInButton.setOnClickListener(this);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
               GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                if(isValidEmail(account.getEmail()) ){
                    firebaseAuthWithGoogle(account.getIdToken());
                }else
                   alertLoginError(getString(R.string.invalid_loginid_alert));

            } catch (ApiException e) {
                // Sign in failed
                alertLoginError(getString(R.string.general_login_failure_error));
            }
        }
    }

    private boolean isValidEmail(String email) {
        boolean validUser = false;
        String emailDomain = email.split("@")[1];

        if(special_users.contains(email) || domains.contains(emailDomain)  )
            validUser = true;
        return validUser;

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
                                dialog.cancel();

                    }
                });
        alertDialog2.show();
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                                addUserToFirebaseDB();
                                Intent newIntent = new Intent(GoogleSignInActivity.this, MainActivity.class);
                                startActivity(newIntent);
                        } else {
                            alertLoginError(getString(R.string.firebase_auth_error));
                            Log.d(TAG, "signInWithCredential:failed");
                        }
                    }

                });
    }



    private void addUserToFirebaseDB() {
        FirebaseDatabase.getInstance().getReference().child("users")
                    .child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference usersRef = dbRef.child("users");

                        String key = currentUser.getUid();
                        usersRef.child(key).child("email").setValue(currentUser.getEmail());
                        usersRef.child(key).child("fullname").setValue(currentUser.getDisplayName());
                        usersRef.child(key).child("isAdmin").setValue(false);
                        usersRef.child(key).child("school").setValue("missionsanjosehigh");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            usersRef.child(key).child("imgurl").setValue(Objects.requireNonNull(currentUser.getPhotoUrl()).toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

    }

    @Override
    public void onClick(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

}
