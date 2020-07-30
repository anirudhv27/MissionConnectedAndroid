package com.avaliveru.missionconnected.ui;

import android.app.Activity;
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
import com.avaliveru.missionconnected.dataModels.Event;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventsDetailsActivity extends AppCompatActivity {

    Event event;
    Button isGoingButton;
    boolean isGoing;

    DatabaseReference rootRef = FirebaseDatabase.getInstance()
            .getReference();
    DatabaseReference myEventNamesRef = rootRef.child("users").child("t8AKiEV08yVulfouZM9xAA1gCCC3").child("events");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        final TextView eventNameTextView = findViewById(R.id.eventDetailsNameTextView);
        final TextView eventDescriptionTextView = findViewById(R.id.eventDetailsDescriptionTextView);
        final ImageView eventImageView = findViewById(R.id.eventDetailsImageView);
        event = new Event();
        isGoingButton = findViewById(R.id.isGoingButton);
        final TextView memberTextView = findViewById(R.id.eventDetailsMemberNumberTextView);

        String eventName = getIntent().getStringExtra("eventName");
        isGoing = getIntent().getBooleanExtra("isGoing", false);
        if (isGoing) {
            isGoingButton.setBackgroundColor(Color.parseColor("#dd0031"));
            isGoingButton.setText("Can't Go");
        } else {
            isGoingButton.setBackgroundColor(Color.parseColor("#3FA33F"));
            isGoingButton.setText("I'm Going!");
        }

        DatabaseReference eventDetailsRef= rootRef
                .child("schools").child("missionsanjosehigh").child("events").child(eventName);
        eventDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                event = new Event();
                event.eventID = snapshot.getKey();
                event.eventDescription = snapshot.child("event_description").getValue().toString();
                event.eventPreview = snapshot.child("event_preview").getValue().toString();
                event.eventImageURL = snapshot.child("event_image_url").getValue().toString();
                event.eventName = snapshot.child("event_name").getValue().toString();
                //event.numberOfAttendees = (long) snapshot.child("member_numbers").getValue();

                eventNameTextView.setText(event.eventName);
                eventDescriptionTextView.setText(event.eventDescription);
                memberTextView.setText("Members: " + new Long(event.numberOfAttendees).toString());
                Glide.with(EventsDetailsActivity.this).load(Uri.parse(event.eventImageURL)).into(eventImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //TODO: Implement what to do when isGoing item is pressed
    public void imGoingButtonActionPressed(View view) {
        String eventID = getIntent().getStringExtra("eventName");

        if (isGoing) {
            myEventNamesRef.child(eventID).child("isGoing").setValue(false);

            rootRef.child("schools").child("missionsanjosehigh")
                    .child("events").child(getIntent().getStringExtra("eventName")).child("member_numbers")
                    .setValue(event.numberOfAttendees - 1);

            event.numberOfAttendees -= 1;

            isGoing = false;
            isGoingButton.setBackgroundColor(Color.parseColor("#3FA33F"));
            isGoingButton.setText("I'm Going!");
        } else {
            myEventNamesRef.child(eventID).child("isGoing").setValue(true);

            rootRef.child("schools").child("missionsanjosehigh")
                    .child("events").child(eventID).child("member_numbers")
                    .setValue(event.numberOfAttendees + 1);
            event.numberOfAttendees += 1;

            isGoing = true;
            isGoingButton.setBackgroundColor(Color.parseColor("#dd0031"));
            isGoingButton.setText("Can't Go");

        }
        finish();
    }
}
