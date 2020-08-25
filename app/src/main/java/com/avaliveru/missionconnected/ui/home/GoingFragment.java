package com.avaliveru.missionconnected.ui.home;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avaliveru.missionconnected.R;
import com.avaliveru.missionconnected.dataModels.Event;
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
import java.util.Date;

public class GoingFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyText;

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
                    if (childSnapshot.child("isGoing").exists()) {
                        if ((boolean) childSnapshot.child("isGoing").getValue()) {
                            eventIDs.add(childSnapshot.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchEventsList() {
        events = new ArrayList<>();
        DatabaseReference eventDetailRef = FirebaseDatabase.getInstance().getReference().child("schools").child("missionsanjosehigh").child("events");
        eventDetailRef.addValueEventListener(new ValueEventListener() {
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
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, -1);

                        Date yesterday = calendar.getTime();

                        if (event.eventDate.after(yesterday)) {
                            events.add(event);
                        }
                    }
                }
                mAdapter = new EventsAdapter(getContext(), events);
                recyclerView.setAdapter(mAdapter);
                if (mAdapter.getItemCount() == 0) {
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    emptyText.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_going, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        recyclerView = root.findViewById(R.id.goingRecyclerView);
        emptyText = root.findViewById(R.id.goingRecyclerView_no_data);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        emptyText.setText(Html.fromHtml(getString(R.string.no_going_events)));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchEventIDs();
        fetchEventsList();
    }

    @Override
    public void onPause() {
        super.onPause();

    }
}
