package com.avaliveru.missionconnected;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GoogleSignInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GoogleSignInActivity";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    //private static final int RESULT_OK = 9002;

    private GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    FirebaseUser currentUser;
    //private ArrayList<String> domains;
    //private ArrayList<String> special_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);


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
        if (requestCode == RC_SIGN_IN && (resultCode == RESULT_OK)) {
            System.out.println("Task");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
               final GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Sign in failed
                alertLoginError(getString(R.string.general_login_failure_error));
            }
        }
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


    private void    firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    currentUser = mAuth.getCurrentUser();
                    checkAndInsertUserToFireDB();
                } else {
                    alertLoginError(getString(R.string.firebase_auth_error));
                    Log.d(TAG, "signInWithCredential:failed");
                }
            }
        });
    }

    private void checkAndInsertUserToFireDB() {
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    //User in Fire DB , goto main activity
                    Intent newIntent = new Intent(GoogleSignInActivity.this, MainActivity.class);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(newIntent);
                }else{
                    //User not in FireDB! Register the user
                    Intent newIntent = new Intent(GoogleSignInActivity.this, PickSchoolActivity.class);
                    startActivity(newIntent);
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
