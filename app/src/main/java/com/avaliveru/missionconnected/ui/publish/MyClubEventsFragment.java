package com.avaliveru.missionconnected.ui.publish;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Event;
import com.avaliveru.missionconnected.ui.EventsDetailsActivity;
import com.avaliveru.missionconnected.ui.home.EventsAdapter;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class MyClubEventsFragment extends Fragment {
    private RecyclerView recyclerView;

    private ArrayList<String> eventIDs;
    private ArrayList<Event> events;
    private RecyclerView.Adapter mAdapter;

    private void fetchEventIDs() {
        eventIDs = new ArrayList<>();
        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference myEventNamesRef = rootRef.child("users").child(currentUser.getUid()).child("events");

        myEventNamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    if (childSnapshot.child("member_status").getValue().toString().equals("Officer")) {
                        eventIDs.add(childSnapshot.getKey());
                    }
                }
                events = new ArrayList<>();
                DatabaseReference eventDetailRef = FirebaseDatabase.getInstance().getReference().child("schools").child("missionsanjosehigh").child("events");
                eventDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            if (eventIDs.contains(childSnapshot.getKey())) {
                                Event event = new Event();
                                event.eventID = childSnapshot.getKey();
                                event.eventImageURL = childSnapshot.child("event_image_url").getValue().toString();
                                event.eventName = childSnapshot.child("event_name").getValue().toString();
                                event.eventClub = childSnapshot.child("event_club").getValue().toString();
                                event.eventDescription = childSnapshot.child("event_description").getValue().toString();
                                event.eventPreview = childSnapshot.child("event_preview").getValue().toString();
                                event.numberOfAttendees = Integer.parseInt(childSnapshot.child("member_numbers").getValue().toString());

                                SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                                try {
                                    event.eventDate = df.parse(childSnapshot.child("event_date").getValue().toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                events.add(event);
                            }
                        }

                        mAdapter = new PublishEventsAdapter(events);
                        mAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchEventsList() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_club_events, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = root.findViewById(R.id.myClubEventsRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        fetchEventIDs();
        fetchEventsList();

        return root;
    }

    //    @Override
//    public void onResume() {
//        super.onResume();
//        fetchEventIDs();
//        fetchEventsList();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        events = new ArrayList<>();
//        eventIDs= new ArrayList<>();
//    }

    private class PublishEventsAdapter extends RecyclerView.Adapter<PublishEventsAdapter.ViewHolder> {
        ArrayList<Event> events;

        public PublishEventsAdapter(ArrayList<Event> events) {
            this.events = events;
            Collections.sort(events);
        }

        @NonNull
        @Override
        public PublishEventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_events_list_publish_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PublishEventsAdapter.ViewHolder holder, int position) {
            final DatabaseReference rootRef = FirebaseDatabase.getInstance()
                    .getReference();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference myEventNamesRef = rootRef.child("users").child(currentUser.getUid()).child("events");
            final Event currEvent = events.get(position);

            rootRef.child("schools").child("missionsanjosehigh").child("clubs").child(currEvent.eventClub).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.setEventClubTitle(snapshot.child("club_name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.setEventClubTitle("No Club Available :(");
                }
            });

            holder.setEventNameTitle(currEvent.eventName);
            holder.setEventDateTitle(currEvent.eventDate);
            holder.setImage(currEvent.eventImageURL);
            holder.event = currEvent;

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(getContext(), EventsDetailsActivity.class);
                    String eventID = currEvent.eventID;
                    intent.putExtra("eventName", eventID);
                    myEventNamesRef.child(eventID).child("isGoing").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isGoing = (boolean) snapshot.getValue();
                            intent.putExtra("isGoing", isGoing);
                            intent.putExtra("isSubscribed", true);
                            getContext().startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public RelativeLayout root;
            public ImageView image;
            public TextView eventName;
            public TextView eventClub;
            public TextView eventDate;
            public Button editButton;

            public Event event;

            public ViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.eventPublishImageView);
                eventName = itemView.findViewById(R.id.eventPublishNameTextField);
                eventClub = itemView.findViewById(R.id.eventPublishClubTextField);
                eventDate = itemView.findViewById(R.id.eventPublishDateTextField);
                root = itemView.findViewById(R.id.event_publish_list_root);

                editButton = itemView.findViewById(R.id.editButton);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Los Angeles"));
                        cal.setTime(event.eventDate);

                        String dateString = (cal.get(Calendar.MONTH) + 1) + "/" +
                                cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);

                        Bundle b = new Bundle();
                        b.putString("clubName", eventClub.getText().toString().trim());
                        b.putString("eventName", event.eventName);
                        b.putString("eventDescription", event.eventDescription);
                        b.putString("eventClubID", event.eventClub);
                        b.putString("eventID", event.eventID);
                        b.putString("eventImageURL", event.eventImageURL);
                        b.putString("eventPreview", event.eventPreview);
                        b.putString("eventDate", dateString);

                        Intent newIntent = new Intent(getContext(), AddEventActivity.class);
                        newIntent.putExtra("bundle", b);
                        newIntent.putExtra("isFromEdit", true);
                        startActivity(newIntent);
                    }
                });
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
}
