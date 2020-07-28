package com.avaliveru.missionconnected.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Club;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClubsDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_details);
        final TextView clubNameTextView = findViewById(R.id.clubDetailsNameTextView);
        final TextView clubDescriptionTextView = findViewById(R.id.clubDetailsDescriptionTextView);
        final ImageView clubImageView = findViewById(R.id.clubDetailsImageView);
        Button allClubsButton = findViewById(R.id.allClubEventsButton);
        final TextView memberTextView = findViewById(R.id.clubDetailsMemberNumberTextView);

        String clubName = getIntent().getStringExtra("clubName");
        DatabaseReference clubDetailsRef= FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("clubs").child(clubName);
        clubDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clubNameTextView.setText(snapshot.child("club_name").getValue().toString());
                clubDescriptionTextView.setText(snapshot.child("club_description").getValue().toString());
                memberTextView.setText("Members: " + snapshot.child("member_numbers").getValue().toString());
                Glide.with(ClubsDetailsActivity.this).load(Uri.parse(snapshot.child("club_image_url").getValue().toString())).into(clubImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
