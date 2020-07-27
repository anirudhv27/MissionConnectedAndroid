package com.avaliveru.missionconnected.ui.home;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Event;
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

public class GoingFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private Set<String> eventNames = new HashSet<>();

    private void fetchEvents() {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        //TODO: Get user id key from Firebase
        final DatabaseReference myEventNamesRef = rootRef.child("users").child("t8AKiEV08yVulfouZM9xAA1gCCC3").child("events");
        myEventNamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                eventNames = map.keySet();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("events");

        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(query, new SnapshotParser<Event>() {
                            @NonNull
                            @Override
                            public Event parseSnapshot(@NonNull DataSnapshot snapshot) {
                                Event event = new Event();
                                event.eventID = snapshot.getKey().toString();
                                event.eventDescription = snapshot.child("event_description").getValue().toString();
                                event.eventPreview = snapshot.child("event_preview").getValue().toString();
                                event.eventImageURL = snapshot.child("event_image_url").getValue().toString();
                                event.eventName = snapshot.child("event_name").getValue().toString();
                                event.eventClub = snapshot.child("event_club").getValue().toString();
                                //event.numberOfAttendees = (long) snapshot.child("member_numbers").getValue();
                                SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                                try {
                                    event.eventDate = df.parse(snapshot.child("event_date").getValue().toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return event;
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Event, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_eventslistitem, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull final Event model) {
                myEventNamesRef.child(model.eventID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isGoing = (boolean) snapshot.child("isGoing").getValue();
                        if (eventNames.contains(model.eventID) && model.eventDate.before(new Date()) && isGoing) {
                            rootRef.child("clubs").child(model.eventClub).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    holder.setEventClubTitle(snapshot.child("club_name").getValue().toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    holder.setEventClubTitle("No Club Available :(");
                                }
                            });
                            holder.setEventDateTitle(model.eventDate);
                            holder.setEventNameTitle(model.eventName);
                            holder.setImage(model.eventImageURL);

                            holder.root.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_going, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        recyclerView = root.findViewById(R.id.goingRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchEvents();

        return root;
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
            Glide.with(getContext()).load(Uri.parse(imageURL)).into(image);
        }
    }
}
