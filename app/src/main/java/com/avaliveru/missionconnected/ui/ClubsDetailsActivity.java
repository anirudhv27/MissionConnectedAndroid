package com.avaliveru.missionconnected.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClubsDetailsActivity extends AppCompatActivity {
    Club club;
    Button subscribeButton;
    boolean isMyClub;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String loginUserId = currentUser.getUid();

    DatabaseReference rootRef = FirebaseDatabase.getInstance()
            .getReference();
    DatabaseReference myClubNamesRef = rootRef.child("users").child(loginUserId).child("clubs");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_details);

        final TextView clubNameTextView = findViewById(R.id.clubDetailsNameTextView);
        final TextView clubDescriptionTextView = findViewById(R.id.clubDetailsDescriptionTextView);
        final ImageView clubImageView = findViewById(R.id.clubDetailsImageView);
        subscribeButton = findViewById(R.id.subscribeButton);
        final TextView memberTextView = findViewById(R.id.clubDetailsMemberNumberTextView);

        String clubName = getIntent().getStringExtra("clubName");
        isMyClub = getIntent().getBooleanExtra("isMyClub", false);
        if (isMyClub) {
            subscribeButton.setBackgroundColor(Color.parseColor("#dd0031"));
            subscribeButton.setText(R.string.club_unsubscribe);
        } else {
            subscribeButton.setBackgroundColor(Color.parseColor("#3FA33F"));
            subscribeButton.setText(R.string.club_subscribe);
        }
        DatabaseReference clubDetailsRef= rootRef
                .child("schools").child("missionsanjosehigh").child("clubs").child(clubName);
        clubDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                club = new Club();
                club.clubID = snapshot.getKey();
                club.clubDescription = (snapshot.child("club_description").getValue() == null)?"" : snapshot.child("club_description").getValue().toString();
                club.clubPreview = (snapshot.child("club_preview").getValue() == null)?"":snapshot.child("club_preview").getValue().toString();
                club.clubImageURL = (snapshot.child("club_image_url").getValue() == null)?"":snapshot.child("club_image_url").getValue().toString();
                club.clubName = (snapshot.child("club_name").getValue() == null)?"":snapshot.child("club_name").getValue().toString();
                club.numberOfMembers = Integer.parseInt((snapshot.child("member_numbers").getValue() == null)?"0":snapshot.child("member_numbers").getValue().toString());

                clubNameTextView.setText(club.clubName);
                clubDescriptionTextView.setText(club.clubDescription);
                clubDescriptionTextView.setMovementMethod(new ScrollingMovementMethod());
                memberTextView.setText("Members: " + club.numberOfMembers);
                if(!club.clubImageURL.equals(""))
                    Glide.with(ClubsDetailsActivity.this).load(Uri.parse(club.clubImageURL)).into(clubImageView);
                else
                    clubImageView.setImageResource(R.mipmap.ic_login_image);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void allClubEventsButtonPressed(View view) {
        String clubID = getIntent().getStringExtra("clubName");
        Intent newIntent = new Intent(ClubsDetailsActivity.this, AllClubEventsActivity.class);
        newIntent.putExtra("clubID", clubID);
        newIntent.putExtra("isSubscribed", isMyClub);
        startActivity(newIntent);
    }

    public void subscribeButtonActionPressed(View view) {
        String alertTitle, alertMessage, alertButton;
        if(isMyClub) {
            alertTitle = "Leave this Club?";
            alertMessage = "Leave " + club.clubName+ "?";
            alertButton = "Leave";
        }else {
            alertTitle = "Join?";
            alertMessage = "Subscribe to "+club.clubName +"?";
            alertButton = "Join";
        }
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder( this);
        alertDialog2.setTitle(alertTitle);
        alertDialog2.setMessage(alertMessage);
        alertDialog2.setPositiveButton(alertButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                subscribeButtonActionPressedConfirmed();
                dialog.cancel();
            }
        });
        alertDialog2.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                finish();
            }
        });
        alertDialog2.show();
    }

    public void subscribeButtonActionPressedConfirmed(){
        final String clubID = getIntent().getStringExtra("clubName");
        if (isMyClub) {
            myClubNamesRef.child(clubID).removeValue();
            DatabaseReference eventsRef = rootRef.child("schools").child("missionsanjosehigh").child("clubs").child(clubID).child("events");
            eventsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot childSnap : snapshot.getChildren()) {
                        String event = childSnap.getKey();
                        rootRef.child("users").child(loginUserId)
                                .child("events").child(event).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
            rootRef.child("schools").child("missionsanjosehigh")
                    .child("clubs").child(getIntent().getStringExtra("clubName")).child("member_numbers")
                    .setValue(club.numberOfMembers - 1);
        } else {
            myClubNamesRef.child(clubID).setValue("Member");
            DatabaseReference eventsRef = rootRef.child("schools").child("missionsanjosehigh").child("clubs").child(clubID).child("events");
            eventsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot childSnap : snapshot.getChildren()) {
                        String event = childSnap.getKey();
                        rootRef.child("users").child(loginUserId)
                                .child("events").child(event).child("member_status").setValue("Member");
                        rootRef.child("users").child(loginUserId)
                                .child("events").child(event).child("isGoing").setValue(false);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
            rootRef.child("schools").child("missionsanjosehigh")
                    .child("clubs").child(clubID).child("member_numbers")
                    .setValue(club.numberOfMembers + 1);
        }
        finish();
    }
}
