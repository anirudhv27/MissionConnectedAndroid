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
        final Query eventDetailRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("schools").child("missionsanjosehigh").child("events").orderByChild("isGoing").equalTo(true);

        FirebaseRecyclerOptions<Boolean> options =
                new FirebaseRecyclerOptions.Builder<Boolean>()
                        .setQuery(myEventNamesRef, new SnapshotParser<Boolean>() {
                            @NonNull
                            @Override
                            public Boolean parseSnapshot(@NonNull DataSnapshot snapshot) {

                                if (snapshot.child("member_status").getValue().toString().equals("Officer") || snapshot.child("member_status").getValue().toString().equals("Member")) {
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
                ((DatabaseReference) eventDetailRef).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.eventID = snapshot.getKey();
                        String eventImageURL = snapshot.child("event_image_url").getValue().toString();
                        String eventName = snapshot.child("event_name").getValue().toString();
                        String eventClub = snapshot.child("event_club").getValue().toString();

                        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                        Date eventDate = null;
                        try {
                            eventDate = df.parse(snapshot.child("event_date").getValue().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

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
            Glide.with(getContext()).load(Uri.parse(imageURL)).into(image);
        }
    }
}
