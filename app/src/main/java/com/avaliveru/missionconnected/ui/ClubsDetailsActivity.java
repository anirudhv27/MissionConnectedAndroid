package com.avaliveru.missionconnected.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
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
            subscribeButton.setText("Unsubscribe");
        } else {
            subscribeButton.setBackgroundColor(Color.parseColor("#3FA33F"));
            subscribeButton.setText("Subscribe");
        }
        DatabaseReference clubDetailsRef= rootRef
                .child("schools").child("missionsanjosehigh").child("clubs").child(clubName);
        clubDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                club = new Club();
                club.clubID = snapshot.getKey();
                club.clubDescription = snapshot.child("club_description").getValue().toString();
                club.clubPreview = snapshot.child("club_preview").getValue().toString();
                club.clubImageURL = snapshot.child("club_image_url").getValue().toString();
                club.clubName = snapshot.child("club_name").getValue().toString();
                club.numberOfMembers = Integer.parseInt(snapshot.child("member_numbers").getValue().toString());

                clubNameTextView.setText(club.clubName);
                clubDescriptionTextView.setText(club.clubDescription);
                memberTextView.setText("Members: " + new Long(club.numberOfMembers).toString());
                Glide.with(ClubsDetailsActivity.this).load(Uri.parse(club.clubImageURL)).into(clubImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void allClubEventsButtonPressed(View view) {
        String clubID = getIntent().getStringExtra("clubName");

        Intent newIntent = new Intent(ClubsDetailsActivity.this, AllClubEventsActivity.class);
        newIntent.putExtra("clubID", clubID);
        startActivity(newIntent);
    }

    public void subscribeButtonActionPressed(View view) {
        String clubID = getIntent().getStringExtra("clubName");
        if (isMyClub) {
            myClubNamesRef.child(clubID).removeValue();

            DatabaseReference eventsRef = rootRef.child("schools").child("missionsanjosehigh").child("clubs").child(clubID).child("events");
            eventsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String event = snapshot.getKey();
                    rootRef.child("users").child(loginUserId)
                            .child("events").child(event).removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            //TODO: fix numberOfMembers because the club is not passed to the listener
            rootRef.child("schools").child("missionsanjosehigh")
                    .child("clubs").child(getIntent().getStringExtra("clubName")).child("member_numbers")
                    .setValue(club.numberOfMembers - 1);
            club.numberOfMembers -= 1;

            isMyClub = false;
            subscribeButton.setBackgroundColor(Color.parseColor("#3FA33F"));
            subscribeButton.setText("Subscribe");
        } else {
            myClubNamesRef.child(clubID).setValue("Member");
            DatabaseReference eventsRef = rootRef.child("schools").child("missionsanjosehigh").child("clubs").child(clubID).child("events");
            eventsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String event = snapshot.getKey();
                    rootRef.child("users").child(loginUserId)
                            .child("events").child(event).child("member_status").setValue("Member");
                    rootRef.child("users").child(loginUserId)
                            .child("events").child(event).child("isGoing").setValue(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            //TODO: fix numberOfMembers because the club is not passed to the listener
            rootRef.child("schools").child("missionsanjosehigh")
                    .child("clubs").child(clubID).child("member_numbers")
                    .setValue(club.numberOfMembers + 1);
            club.numberOfMembers += 1;

            isMyClub = true;
            subscribeButton.setBackgroundColor(Color.parseColor("#dd0031"));
            subscribeButton.setText("Unsubscribe");

        }
        finish();
    }
}
