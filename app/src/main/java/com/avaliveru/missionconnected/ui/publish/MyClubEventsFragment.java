package com.avaliveru.missionconnected.ui.publish;

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
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyClubEventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private void fetchEvents() {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance()
                .getReference();
        //TODO: Get user id key from Firebase
        final DatabaseReference myEventNamesRef = rootRef.child("users").child("t8AKiEV08yVulfouZM9xAA1gCCC3").child("events");
        final DatabaseReference eventDetailRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("events");

        //TODO 1: Figure out some way to only find events with date after today ,might need to adjust database :(
        FirebaseRecyclerOptions<Boolean> options =
                new FirebaseRecyclerOptions.Builder<Boolean>()
                        .setQuery(myEventNamesRef, new SnapshotParser<Boolean>() {
                            @NonNull
                            @Override
                            public Boolean parseSnapshot(@NonNull DataSnapshot snapshot) {

                                if (snapshot.child("member_status").getValue().toString().equals("Officer")) {
                                    return true;
                                } else {
                                    return false;
                                }
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
                        holder.event = new Event();
                        holder.event.eventID = snapshot.getKey();

                        String eventImageURL = "https://www.androidpolice.com/wp-content/uploads/2015/03/nexus2cee_an.png";
                        try {
                            eventImageURL = snapshot.child("event_image_url").getValue().toString();
                            holder.event.eventImageURL = eventImageURL;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String eventName = snapshot.child("event_name").getValue().toString();
                        holder.event.eventName = eventName;
                        String eventClub = snapshot.child("event_club").getValue().toString();
                        holder.event.eventClub = eventClub;

                        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                        Date eventDate = null;
                        try {
                            eventDate = df.parse(snapshot.child("event_date").getValue().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        holder.event.eventDate = eventDate;
                        holder.event.eventPreview = snapshot.child("event_preview").getValue().toString();
                        holder.event.eventDescription = snapshot.child("event_description").getValue().toString();


                        rootRef.child("clubs").child(eventClub).addValueEventListener(new ValueEventListener() {
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
                                final Intent intent = new Intent(MyClubEventsFragment.this.getContext(), EventsDetailsActivity.class);
                                String eventID = snapshot.getKey();
                                intent.putExtra("eventName", eventID);
                                myEventNamesRef.child(eventID).child("isGoing").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean isGoing = (boolean) snapshot.getValue();
                                        intent.putExtra("isGoing", isGoing);
                                        MyClubEventsFragment.this.startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_events_list_publish_item, parent, false);
                return new ViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_club_events, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = root.findViewById(R.id.myClubEventsRecyclerView);
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
                    PublishFragment pubFrag = (PublishFragment) getParentFragment();
                    pubFrag.viewPager.setCurrentItem(1);

                    //viewPager.setCurrentItem(1);
                    pubFrag.tabLayout.getTabAt(1).select();
                    AddEventFragment addEventFragment = (AddEventFragment) pubFrag.pagerAdapter.createFragment(1);

                    //addEventFragment.currClubID = event.eventClub;
                    //addEventFragment.currClubName = eventClub.getText().toString();
                    //addEventFragment.eventName.getEditText().setText(event.eventName);
                    //addEventFragment.eventPreview.getEditText().setText(event.eventPreview);

                    //Glide.with(pubFrag.getActivity()).load(event.eventImageURL).into(addEventFragment.eventImageButton);
                    //addEventFragment.mImageUri = Uri.parse(event.eventImageURL);

                    //addEventFragment.eventID = event.eventID;
                    //addEventFragment.eventDescription.getEditText().setText(event.eventDescription);

                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Los Angeles"));
                    cal.setTime(event.eventDate);

                    String dateString = cal.get(Calendar.MONTH) + "/" +
                            cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);

                    //addEventFragment.eventDate.getEditText().setText(dateString);

                    Bundle b = new Bundle();
                    b.putString("clubName", eventClub.getText().toString());
                    b.putString("eventName", event.eventName);
                    b.putString("eventDescription", event.eventDescription);
                    b.putString("eventClubID", event.eventClub);
                    b.putString("eventID", event.eventID);
                    b.putString("eventImageURL", event.eventImageURL);
                    b.putString("eventPreview", event.eventPreview);
                    b.putString("eventDate", dateString);
                    b.putBoolean("isFromEdit", true);

                    addEventFragment.setArguments(b);

                    //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.addEventFragment, addEventFragment).commit();
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
