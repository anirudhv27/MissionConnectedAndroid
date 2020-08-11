package com.avaliveru.missionconnected.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Event;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventsDetailsActivity extends AppCompatActivity {
    Event event;
    Button isGoingButton;
    boolean isGoing;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference rootRef = FirebaseDatabase.getInstance()
            .getReference();
    DatabaseReference myEventNamesRef = rootRef.child("users").child(currentUser.getUid()).child("events");

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
            isGoingButton.setText(R.string.eventcannotgo);
        } else {
            isGoingButton.setBackgroundColor(Color.parseColor("#3FA33F"));
            isGoingButton.setText(R.string.eventcango);
        }

        DatabaseReference eventDetailsRef= rootRef
                .child("schools").child("missionsanjosehigh").child("events").child(eventName);
        eventDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                event = new Event();
                event.eventID = snapshot.getKey();
                event.eventDescription = (snapshot.child("event_description").getValue() == null)?"":snapshot.child("event_description").getValue().toString();
                event.eventPreview = (snapshot.child("event_preview").getValue()==null)?"":snapshot.child("event_preview").getValue().toString();
                event.eventImageURL = (snapshot.child("event_image_url").getValue() == null)?"":snapshot.child("event_image_url").getValue().toString();
                event.eventName = (snapshot.child("event_name").getValue() == null)? "":snapshot.child("event_name").getValue().toString();
                event.numberOfAttendees = Integer.parseInt((snapshot.child("member_numbers").getValue()== null)?"0":snapshot.child("member_numbers").getValue().toString());

                eventNameTextView.setText(event.eventName);
                eventDescriptionTextView.setText(event.eventDescription);
                memberTextView.setText("Number of Attendees: " + event.numberOfAttendees);
                if(!event.eventImageURL.equals(""))
                 Glide.with(EventsDetailsActivity.this).load(Uri.parse(event.eventImageURL)).into(eventImageView);
                else
                    eventImageView.setImageResource(R.mipmap.ic_login_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void imGoingButtonActionPressed( View v) {

        String alertTitle, alertMessage, alertButton;
        if(isGoing) {
            alertTitle = "Leave this event?";
            alertMessage = "Leave" + event.eventName + "?";
            alertButton = "Leave";
        }else {
            alertTitle = "Join?";
            alertMessage = "Are you going to "+event.eventName+"?";
            alertButton = "Join";
        }
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder( this);
        alertDialog2.setTitle(alertTitle);
        alertDialog2.setMessage(alertMessage);
        alertDialog2.setPositiveButton(alertButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                imGoingButtonActionPressedConfirmed();
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


   private void imGoingButtonActionPressedConfirmed() {
        String eventID = getIntent().getStringExtra("eventName");

        if (isGoing) {
            myEventNamesRef.child(eventID).child("isGoing").setValue(false);
            rootRef.child("schools").child("missionsanjosehigh")
                    .child("events").child(eventID).child("member_numbers")
                    .setValue(event.numberOfAttendees - 1);
        } else {
            myEventNamesRef.child(eventID).child("isGoing").setValue(true);
            rootRef.child("schools").child("missionsanjosehigh")
                    .child("events").child(eventID).child("member_numbers")
                    .setValue(event.numberOfAttendees + 1);
        }
        finish();
    }
}
