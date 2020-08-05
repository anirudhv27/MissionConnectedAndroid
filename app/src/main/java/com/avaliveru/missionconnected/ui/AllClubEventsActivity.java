package com.avaliveru.missionconnected.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Event;
import com.avaliveru.missionconnected.ui.EventsDetailsActivity;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllClubEventsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private String clubID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_club_events);

        clubID = getIntent().getStringExtra("clubID");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = findViewById(R.id.allClubEventsRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchEvents();
    }

    private void fetchEvents() {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        //TODO: Get user id key from Firebase
        final DatabaseReference myEventNamesRef = FirebaseDatabase.getInstance()
                .getReference().child("schools").child("missionsanjosehigh").child("clubs").child(clubID).child("events");
        final DatabaseReference eventDetailRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("events");

        FirebaseRecyclerOptions<Boolean> options =
                new FirebaseRecyclerOptions.Builder<Boolean>()
                        .setQuery(myEventNamesRef, new SnapshotParser<Boolean>() {
                            @NonNull
                            @Override
                            public Boolean parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return true;
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Boolean, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Boolean model) {
                String key = this.getRef(position).getKey();
                eventDetailRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {

                        holder.eventID = snapshot.getKey();
                        String eventImageURL = "https://www.androidpolice.com/wp-content/uploads/2015/03/nexus2cee_an.png";
                        try {
                            eventImageURL = snapshot.child("event_image_url").getValue().toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String eventName = snapshot.child("event_name").getValue().toString();
                        String eventClub = snapshot.child("event_club").getValue().toString();

                        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                        Date eventDate = null;
                        try {
                            eventDate = df.parse(snapshot.child("event_date").getValue().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        rootRef.child("schools").child("missionsanjosehigh").child("clubs").child(eventClub).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.setEventClubTitle(snapshot.child("club_name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                holder.setEventClubTitle("No Club Available :(");
                            }
                        });

                        holder.setEventDateTitle(eventDate);
                        holder.setEventNameTitle(eventName);
                        holder.setImage(eventImageURL);

                        holder.root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent intent = new Intent(AllClubEventsActivity.this, EventsDetailsActivity.class);
                                String eventID = snapshot.getKey();
                                intent.putExtra("eventName", eventID);
                                intent.putExtra("isGoing", false);
                                AllClubEventsActivity.this.startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_eventslistitem, parent, false);
                return new ViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout root;
        public ImageView image;
        public TextView eventName;
        public TextView eventClub;
        public TextView eventDate;
        public String eventID;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.eventImageView);
            eventName = itemView.findViewById(R.id.eventNameTextField);
            eventClub = itemView.findViewById(R.id.eventClubTextField);
            eventDate = itemView.findViewById(R.id.eventDateTextField);
            root = itemView.findViewById(R.id.event_list_root);
        }

        public void setEventNameTitle(String string) {
            eventName.setText(string);
        }
        public void setEventClubTitle(String string) {
            eventClub.setText(string);
        }

        public void setEventDateTitle(Date date) {
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            eventDate.setText(df.format(date));
        }

        public void setImage(String imageURL) {
            Glide.with(AllClubEventsActivity.this).load(Uri.parse(imageURL)).into(image);
        }
    }
}